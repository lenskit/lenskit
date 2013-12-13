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
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class LenskitEvalJob extends TrainTestJob {
    private final DAGNode<CachedSatisfaction, DesireChain> recommenderGraph;

    private LenskitRecommender recommender;

    LenskitEvalJob(@Nonnull AlgorithmInstance algo,
                   @Nonnull TTDataSet ds,
                   @Nonnull MeasurementSuite measures,
                   @Nonnull ExperimentOutputs out,
                   DAGNode<CachedSatisfaction, DesireChain> graph) {
        super(algo, ds, measures, out);
        recommenderGraph = graph;
    }

    @Override
    protected void buildRecommender() throws RecommenderBuildException {
        Preconditions.checkState(recommender == null, "recommender already built");
        RecommenderInstantiator ri = RecommenderInstantiator.create(recommenderGraph);
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
    }
}
