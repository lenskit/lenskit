package org.lenskit.eval.traintest.metrics;

import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Base class for metrics that measure individual recommendation or prediction results.
 *
 * @param <X> The context type.
 */
public abstract class ResultMetric<X> extends Metric<X> {
    /**
     * Construct a new result metric.
     * @param labels Column labels.
     * @param aggLabels Aggregate column labels.
     */
    protected ResultMetric(List<String> labels, List<String> aggLabels) {
        super(labels, aggLabels);
    }

    /**
     * Construct a new result metric.
     * @param resType The result type for measuring results, or `null` for no measurement.
     * @param aggType The result type for aggregate measurements, or `null` for no measurement.
     */
    protected ResultMetric(Class<? extends TypedMetricResult> resType,
                           Class<? extends TypedMetricResult> aggType) {
        super(TypedMetricResult.getColumns(resType),
              TypedMetricResult.getColumns(aggType));
    }

    /**
     * Measure a single result.  The result may come from either prediction or recommendation.
     * @param userId The user ID.
     * @param result The result to measure.
     * @return A list of fields to add to the result's output.
     */
    @Nonnull
    public abstract MetricResult measureResult(long userId, Result result, X context);
}
