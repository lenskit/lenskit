/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.eval.traintest;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.inject.GraphtUtils;
import org.grouplens.lenskit.inject.NodeProcessors;
import org.grouplens.lenskit.inject.RecommenderInstantiator;
import org.grouplens.lenskit.util.table.RowBuilder;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.lenskit.LenskitRecommender;
import org.lenskit.data.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Individual job evaluating a single experimental condition.
 */
class ExperimentJob implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentJob.class);

    private final TrainTestExperiment experiment;
    private final AlgorithmInstance algorithm;
    private final DataSet dataSet;

    private DAGNode<Component, Dependency> recommenderGraph;
    @Nullable
    private final ComponentCache cache;

    ExperimentJob(TrainTestExperiment exp,
                  @Nonnull AlgorithmInstance algo,
                  @Nonnull DataSet ds,
                  @Nonnull DAGNode<Component, Dependency> graph,
                  @Nullable ComponentCache cache) {
        experiment = exp;
        algorithm = algo;
        dataSet = ds;
        recommenderGraph = graph;
        this.cache = cache;
    }

    @Override
    public void run() {
        TableWriter globalOutput = experiment.getGlobalOutput();
        TableWriter userOutput = experiment.getUserOutput();
        RowBuilder outputRow = globalOutput.getLayout().newRowBuilder();

        logger.info("Building {} on {}", algorithm, dataSet);
        Stopwatch buildTimer = Stopwatch.createStarted();
        LenskitRecommender rec = buildRecommender();
        buildTimer.stop();
        logger.info("Built {} in {}", algorithm.getName(), buildTimer);

        logger.info("Measuring {} on {}", algorithm.getName(), dataSet.getName());

        RowBuilder userRow = userOutput != null ? userOutput.getLayout().newRowBuilder() : null;

        Stopwatch testTimer = Stopwatch.createStarted();

        List<ConditionEvaluator> accumulators = Lists.newArrayList();

        for (EvalTask eval: experiment.getTasks()) {
            accumulators.add(eval.createConditionEvaluator(algorithm, dataSet, rec));
        }

        LongSet testUsers = dataSet.getTestData().getUserDAO().getUserIds();
        UserEventDAO userEvents = dataSet.getTestData().getUserEventDAO();
        final NumberFormat pctFormat = NumberFormat.getPercentInstance();
        pctFormat.setMaximumFractionDigits(2);
        pctFormat.setMinimumFractionDigits(2);
        final int nusers = testUsers.size();
        logger.info("Testing {} on {} ({} users)", algorithm, dataSet, nusers);
        int ndone = 0;
        for (LongIterator iter = testUsers.iterator(); iter.hasNext();) {
            if (Thread.interrupted()) {
                throw new RuntimeException("eval job interrupted");
            }
            long uid = iter.nextLong();
            if (userRow != null) {
                userRow.add("User", uid);
            }

            UserHistory<Event> userData = userEvents.getEventsForUser(uid);

            Stopwatch userTimer = Stopwatch.createStarted();

            for (ConditionEvaluator eval : accumulators) {
                Map<String, Object> ures = eval.measureUser(userData);
                if (userRow != null) {
                    userRow.addAll(ures);
                }
            }
            userTimer.stop();
            if (userRow != null) {
                userRow.add("TestTime", userTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
                assert userOutput != null;
                try {
                    userOutput.writeRow(userRow.buildList());
                } catch (IOException e) {
                    throw new EvaluationException("error writing user row", e);
                }
                userRow.clear();
            }

            ndone += 1;
            if (ndone % 100 == 0) {
                double time = testTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001;
                double tpu = time / ndone;
                double tleft = (nusers - ndone) * tpu;
                logger.info("tested {} of {} users ({}), ETA {}",
                            ndone, nusers, pctFormat.format(((double) ndone) / nusers),
                            DurationFormatUtils.formatDurationHMS((long) tleft));
            }
        }
        testTimer.stop();
        logger.info("Tested {} in {}", algorithm.getName(), testTimer);
        outputRow.add("BuildTime", buildTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
        outputRow.add("TestTime", testTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
        for (ConditionEvaluator eval: accumulators) {
            outputRow.addAll(eval.finish());
        }
        try {
            globalOutput.writeRow(outputRow.buildList());
        } catch (IOException e) {
            throw new EvaluationException("error writing output row", e);
        }
    }

    private LenskitRecommender buildRecommender() throws RecommenderBuildException {
        logger.debug("Starting recommender build");
        DAGNode<Component, Dependency> graph;
        if (cache == null) {
            logger.debug("Building directly without a cache");
            RecommenderInstantiator ri = RecommenderInstantiator.create(recommenderGraph);
            graph = ri.instantiate();
        } else {
            logger.debug("Instantiating graph with a cache");
            try {
                Set<DAGNode<Component, Dependency>> nodes = GraphtUtils.getShareableNodes(recommenderGraph);
                logger.debug("resolving {} nodes", nodes.size());
                graph = NodeProcessors.processNodes(recommenderGraph, nodes, cache);
                logger.debug("graph went from {} to {} nodes",
                             recommenderGraph.getReachableNodes().size(),
                             graph.getReachableNodes().size());
            } catch (InjectionException e) {
                logger.error("Error encountered while pre-processing algorithm components for sharing", e);
                throw new RecommenderBuildException("Pre-processing of algorithm components for sharing failed.", e);
            }
        }
        return new LenskitRecommender(graph);
    }
}
