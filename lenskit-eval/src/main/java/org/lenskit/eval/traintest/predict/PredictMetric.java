package org.lenskit.eval.traintest.predict;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.data.history.UserHistory;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.data.events.Event;
import org.lenskit.eval.traintest.metrics.Metric;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Base class for metrics that measure predictions for users.
 *
 * @param <X> The context type.
 */
public abstract class PredictMetric<X> extends Metric<X> {
    /**
     * Construct a new result metric.
     * @param labels Column labels.
     * @param aggLabels Aggregate column labels.
     */
    protected PredictMetric(List<String> labels, List<String> aggLabels) {
        super(labels, aggLabels);
    }

    /**
     * Construct a new result metric.
     * @param resType The result type for measuring results, or `null` for no measurement.
     * @param aggType The result type for aggregate measurements, or `null` for no measurement.
     */
    protected PredictMetric(Class<? extends TypedMetricResult> resType,
                            Class<? extends TypedMetricResult> aggType) {
        super(TypedMetricResult.getColumns(resType),
              TypedMetricResult.getColumns(aggType));
    }

    /**
     * Measure a single result.  The result may come from either prediction or recommendation.
     * @param user The user's test data.
     * @param ratings The user's ratings.
     * @param predictions The predictions.
     * @return A list of fields to add to the result's output.
     */
    @Nonnull
    public abstract MetricResult measureUser(UserHistory<Event> user,
                                             Long2DoubleMap ratings,
                                             ResultMap predictions,
                                             X context);
}
