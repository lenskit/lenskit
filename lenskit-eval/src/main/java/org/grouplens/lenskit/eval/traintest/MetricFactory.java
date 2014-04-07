package org.grouplens.lenskit.eval.traintest;

import org.grouplens.lenskit.eval.metrics.Metric;

import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class MetricFactory {
    public abstract Metric createMetric(TrainTestEvalTask task);

    public abstract List<String> getColumnLabels();

    public abstract List<String> getUserColumnLabels();

    public static MetricFactory forMetric(Metric m) {
        return new Preinstantiated(m);
    }

    private static class Preinstantiated extends MetricFactory {
        private final Metric metric;

        private Preinstantiated(Metric m) {
            this.metric = m;
        }

        @Override
        public Metric createMetric(TrainTestEvalTask task) {
            return metric;
        }

        @Override
        public List<String> getColumnLabels() {
            return metric.getColumnLabels();
        }

        @Override
        public List<String> getUserColumnLabels() {
            return metric.getUserColumnLabels();
        }
    }
}
