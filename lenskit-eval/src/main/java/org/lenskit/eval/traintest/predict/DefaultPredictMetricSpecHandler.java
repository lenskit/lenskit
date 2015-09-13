package org.lenskit.eval.traintest.predict;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auto.service.AutoService;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.eval.traintest.metrics.MetricLoaderHelper;
import org.lenskit.specs.AbstractSpec;
import org.lenskit.specs.DynamicSpec;
import org.lenskit.specs.SpecHandler;

import java.io.IOException;

/**
 * Default spec handler for predict metrics.  It handles all the default metrics, and can instantiate additional
 * type-only metrics from <tt>META-INF/lenskit/predict-metrics.properties</tt> files on the classpath.
 */
@AutoService(SpecHandler.class)
public class DefaultPredictMetricSpecHandler implements SpecHandler {
    private MetricLoaderHelper helper;

    public DefaultPredictMetricSpecHandler() throws IOException {
        helper = new MetricLoaderHelper(ClassLoaders.inferDefault(DefaultPredictMetricSpecHandler.class),
                                        "predict-metrics");
    }

    /**
     * Get the metric type name from a node.  If the node is a string, the string is returned; otherwise, the object's
     * `type` property is returned.
     * @param node The node.
     * @return The type name.
     */
    public static String getMetricTypeName(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            return obj.get("type").asText();
        } else {
            return null;
        }
    }

    @Override
    public <T> T build(Class<T> type, AbstractSpec spec) {
        if (!PredictMetric.class.isAssignableFrom(type)) {
            return null;
        }
        if (!(spec instanceof DynamicSpec)) {
            return null;
        }

        JsonNode node = ((DynamicSpec) spec).getJSON();
        String typeName = getMetricTypeName(node);
        if (typeName == null) {
            return null;
        }

        Object metric = helper.tryInstantiate(typeName);
        if (metric != null) {
            return type.cast(metric);
        }

        return null;
    }
}
