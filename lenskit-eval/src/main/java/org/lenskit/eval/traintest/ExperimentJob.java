/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.MergePool;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.Rating;
import org.lenskit.inject.GraphtUtils;
import org.lenskit.inject.NodeProcessors;
import org.lenskit.inject.RecommenderInstantiator;
import org.lenskit.util.ProgressLogger;
import org.lenskit.util.UncheckedInterruptException;
import org.lenskit.util.table.RowBuilder;
import org.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * Individual job evaluating a single experimental condition.
 */
class ExperimentJob extends RecursiveAction {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentJob.class);

    private final TrainTestExperiment experiment;
    private final AlgorithmInstance algorithm;
    private final DataSet dataSet;
    private final LenskitConfiguration sharedConfig;

    @Nullable
    private final ComponentCache cache;
    private final MergePool<Component, Dependency> mergePool;

    ExperimentJob(TrainTestExperiment exp,
                  @Nonnull AlgorithmInstance algo,
                  @Nonnull DataSet ds,
                  LenskitConfiguration shared,
                  @Nullable ComponentCache cache,
                  @Nullable MergePool<Component, Dependency> pool) {
        experiment = exp;
        algorithm = algo;
        dataSet = ds;
        sharedConfig = shared;
        this.cache = cache;
        mergePool = pool;
    }

    @Override
    protected void compute() {
        ExperimentOutputLayout layout = experiment.getOutputLayout();
        TableWriter globalOutput = layout.prefixTable(experiment.getGlobalOutput(),
                                                      dataSet, algorithm);
        TableWriter userOutput = layout.prefixTable(experiment.getUserOutput(),
                                                    dataSet, algorithm);
        RowBuilder outputRow = globalOutput.getLayout().newRowBuilder();

        logger.debug("fetching training data");
        DataAccessObject trainData = dataSet.getTrainingData().get();
        logger.info("Building {} on {}", algorithm, dataSet);
        Stopwatch buildTimer = Stopwatch.createStarted();
        try (LenskitRecommender rec = buildRecommender(trainData)) {
            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Measuring {} on {}", algorithm.getName(), dataSet.getName());

            RowBuilder userRow = userOutput != null ? userOutput.getLayout().newRowBuilder() : null;


            List<ConditionEvaluator> accumulators = Lists.newArrayList();

            for (EvalTask task : experiment.getTasks()) {
                ConditionEvaluator ce = task.createConditionEvaluator(algorithm, dataSet, rec);
                if (ce != null) {
                    accumulators.add(ce);
                } else {
                    logger.warn("Could not instantiate task {} for algorithm {} on data set {}",
                                task, algorithm, dataSet);
                }
            }

            DataAccessObject testData = dataSet.getTestData().get();

            Stopwatch testTimer = Stopwatch.createStarted();

            final NumberFormat pctFormat = NumberFormat.getPercentInstance();
            pctFormat.setMaximumFractionDigits(2);
            pctFormat.setMinimumFractionDigits(2);
            final int nusers = testData.query(CommonTypes.USER).count();
            logger.info("Testing {} on {} ({} users)", algorithm, dataSet, nusers);
            ProgressLogger progress = ProgressLogger.create(logger)
                                                    .setCount(nusers)
                                                    .setLabel("testing users")
                                                    .start();
            // TODO Support something other than ratings
            for (Entity user: testData.query(CommonTypes.USER).get()) {
                if (Thread.interrupted()) {
                    throw new EvaluationException("eval job interrupted");
                }
                long uid = user.getId();
                if (userRow != null) {
                    userRow.add("User", uid);
                }


                List<Rating> userTrainHistory = trainData.query(Rating.class)
                                                         .withAttribute(CommonAttributes.USER_ID, uid)
                                                         .get();
                List<Rating> userTestHistory = testData.query(Rating.class)
                                                       .withAttribute(CommonAttributes.USER_ID, uid)
                                                       .get();
                TestUser testUser = new TestUser(user, userTrainHistory, userTestHistory);

                Stopwatch userTimer = Stopwatch.createStarted();

                for (ConditionEvaluator eval : accumulators) {
                    Map<String, Object> ures = eval.measureUser(testUser);
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

                progress.advance();
            }

            progress.finish();
            testTimer.stop();
            logger.info("Tested {} in {}", algorithm.getName(), testTimer);
            outputRow.add("BuildTime", buildTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
            outputRow.add("TestTime", testTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
            for (ConditionEvaluator eval : accumulators) {
                outputRow.addAll(eval.finish());
            }
        } catch (UncheckedInterruptException ex) {
            logger.info("evaluation interrupted");
            throw ex;
        } catch (Throwable th) {
            logger.error("Error evaluating " + algorithm + " on " + dataSet, th);
            throw th;
        }

        try {
            globalOutput.writeRow(outputRow.buildList());
        } catch (IOException e) {
            throw new EvaluationException("error writing output row", e);
        }
    }

    private LenskitRecommender buildRecommender(DataAccessObject dao) throws RecommenderBuildException {
        logger.debug("Starting recommender build");
        LenskitConfiguration extraConfig = new LenskitConfiguration();
        extraConfig.addComponent(dao);
        PreferenceDomain dom = dataSet.getTrainingData().getPreferenceDomain();
        if (dom != null) {
            extraConfig.addComponent(dom);
        }

        DAGNode<Component, Dependency> cfgGraph = algorithm.buildRecommenderGraph(sharedConfig, extraConfig);
        if (mergePool != null) {
            logger.debug("deduplicating configuration graph");
            synchronized (mergePool) {
                cfgGraph = mergePool.merge(cfgGraph);
            }
        }
        DAGNode<Component, Dependency> graph;
        if (cache == null) {
            logger.debug("Building directly without a cache");
            RecommenderInstantiator ri = RecommenderInstantiator.create(cfgGraph);
            graph = ri.instantiate();
        } else {
            logger.debug("Instantiating graph with a cache");
            try {
                Set<DAGNode<Component, Dependency>> nodes = GraphtUtils.getShareableNodes(cfgGraph);
                logger.debug("resolving {} nodes", nodes.size());
                graph = NodeProcessors.processNodes(cfgGraph, nodes, cache);
                logger.debug("graph went from {} to {} nodes",
                             cfgGraph.getReachableNodes().size(),
                             graph.getReachableNodes().size());
            } catch (InjectionException e) {
                logger.error("Error encountered while pre-processing algorithm components for sharing", e);
                throw new RecommenderBuildException("Pre-processing of algorithm components for sharing failed.", e);
            }
        }
        return new LenskitRecommender(graph);
    }

    /**
     * Execute this job immediately.
     */
    public void execute() {
        compute();
    }
}
