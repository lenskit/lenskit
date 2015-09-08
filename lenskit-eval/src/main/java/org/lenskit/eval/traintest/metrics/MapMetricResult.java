package org.lenskit.eval.traintest.metrics;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metric result containing arbitrary fields in a map.
 */
class MapMetricResult extends MetricResult {
    private final Map<String, Object> values;

    public MapMetricResult(Map<String,?> vals) {
        values = new LinkedHashMap<>(vals);
    }

    @Override
    public Map<String, Object> getValues() {
        return values;
    }
}
