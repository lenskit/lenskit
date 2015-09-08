package org.lenskit.eval.traintest.metrics;

import java.util.Collections;
import java.util.Map;

/**
 * Class containing metric results.
 */
public abstract class MetricResult {
    /**
     * Get the column values for this metric result.
     * @return The values for the result.
     */
    public abstract Map<String,Object> getValues();

    /**
     * Create an empty metric result.
     * @return An empty metric result.
     */
    public static MetricResult empty() {
        return fromMap(Collections.<String, Object>emptyMap());
    }

    /**
     * Create an empty metric result.
     * @return An empty metric result.
     */
    public static MetricResult fromMap(Map<String,?> values) {
        return new MapMetricResult(values);
    }
}
