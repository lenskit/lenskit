package org.grouplens.lenskit.eval.traintest

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import org.junit.Test
import org.junit.Before
import org.grouplens.lenskit.eval.config.BuilderDelegate
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.config.ConfigBlockDelegate

/**
 * Tests for train-test configurations; they also serve to test the builder delegate
 * framework.
 * @author Michael Ekstrand
 */
class TestTrainTestConfig {
    TrainTestEvalBuilder builder
    ConfigBlockDelegate delegate

    @Before
    void setupDelegate() {
        builder = new TrainTestEvalBuilder()
        delegate = new BuilderDelegate(builder)
    }

    @Test
    void testAddMetric() {
        delegate.apply {
            metric new CoveragePredictMetric()
            metric new RMSEPredictMetric()
        }
        def metrics = builder.getMetrics()
        assertThat(metrics, hasSize(2))
    }

    @Test
    void testAddMetricsByClass() {
        delegate.apply {
            metric CoveragePredictMetric
            metric RMSEPredictMetric
        }
        def metrics = builder.getMetrics()
        assertThat(metrics, hasSize(2))
    }
}
