/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.inject.RecommenderInstantiator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class LenskitEvalJob extends TrainTestJob {
    private DAGNode<CachedSatisfaction, DesireChain> recommenderGraph;
    @Nullable
    private final ComponentCache cache;

    private LenskitRecommender recommender;

    LenskitEvalJob(TrainTestEvalTask task,
                   @Nonnull AlgorithmInstance algo,
                   @Nonnull TTDataSet ds,
                   @Nonnull MeasurementSuite measures,
                   @Nonnull ExperimentOutputs out,
                   DAGNode<CachedSatisfaction, DesireChain> graph,
                   @Nullable ComponentCache cache) {
        super(task, algo, ds, measures, out);
        recommenderGraph = graph;
        this.cache = cache;
    }

    @Override
    protected void buildRecommender() throws RecommenderBuildException {
        Preconditions.checkState(recommender == null, "recommender already built");
        RecommenderInstantiator ri;
        if (cache == null) {
            ri = RecommenderInstantiator.create(recommenderGraph);
        } else {
            ri = RecommenderInstantiator.create(recommenderGraph,
                                                cache.makeInstantiator(recommenderGraph));
        }
        DAGNode<CachedSatisfaction, DesireChain> graph = ri.instantiate();
        recommender = new LenskitRecommender(graph);
    }

    @Override
    protected List<Object> getModelMeasurements() {
        List<Object> row = Lists.newArrayList();
        for (ModelMetric metric: measurements.getModelMetrics()) {
            row.addAll(metric.measureAlgorithm(algorithmInfo, dataSet, recommender));
        }
        return row;
    }

    @Override
    protected TestUser getUserResults(long uid) {
        Preconditions.checkState(recommender != null, "recommender not built");
        UserHistory<Event> userData = dataSet.getTestData().getUserEventDAO().getEventsForUser(uid);
        return new LenskitTestUser(recommender, userData);
    }

    @Override
    protected void cleanup() {
        recommender = null;
        recommenderGraph = null;
    }
}
