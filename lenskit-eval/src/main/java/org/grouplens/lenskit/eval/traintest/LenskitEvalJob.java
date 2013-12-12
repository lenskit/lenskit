package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class LenskitEvalJob extends TrainTestJob {
    private final Provider<PreferenceSnapshot> snapshot;
    private final AlgorithmInstance algorithm;

    private LenskitRecommender recommender;

    LenskitEvalJob(@Nonnull AlgorithmInstance algo,
                   @Nonnull TTDataSet ds,
                   @Nonnull MeasurementSuite measures,
                   @Nonnull ExperimentOutputs out,
                   Provider<PreferenceSnapshot> snap) {
        super(algo, ds, measures, out);
        algorithm = algo;
        snapshot = snap;
    }

    @Override
    protected void buildRecommender() throws RecommenderBuildException {
        Preconditions.checkState(recommender == null, "recommender already built");
        ExecutionInfo execInfo = buildExecInfo();
        LenskitConfiguration config = new LenskitConfiguration();
        config.addComponent(execInfo);
        PreferenceDomain domain = dataSet.getPreferenceDomain();
        if (domain != null) {
            config.addComponent(domain);
        }
        config.addComponent(dataSet.getTrainingDAO());
        config.bind(PreferenceSnapshot.class).toProvider(snapshot);
        // FIXME Add the RNG
        recommender = algorithm.buildRecommender(config);
    }

    private ExecutionInfo buildExecInfo() {
        ExecutionInfo.Builder bld = new ExecutionInfo.Builder();
        bld.setAlgoName(algorithmInfo.getName())
           .setAlgoAttributes(algorithmInfo.getAttributes())
           .setDataName(dataSet.getName())
           .setDataAttributes(dataSet.getAttributes());
        return bld.build();
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
