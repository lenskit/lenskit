package org.grouplens.lenskit.eval.traintest

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*

import org.junit.Test
import org.junit.Before
import org.grouplens.lenskit.eval.config.BuilderDelegate
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.config.ConfigBlockDelegate
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet
import org.grouplens.lenskit.eval.data.CSVDataSource
import org.grouplens.lenskit.eval.config.EvalConfigEngine

/**
 * Tests for train-test configurations; they also serve to test the builder delegate
 * framework.
 * @author Michael Ekstrand
 */
class TestTrainTestBuilderConfig {
    EvalConfigEngine engine
    TrainTestEvalBuilder builder
    ConfigBlockDelegate delegate

    @Before
    void setupDelegate() {
        engine = new EvalConfigEngine()
        builder = new TrainTestEvalBuilder()
        delegate = new BuilderDelegate(engine, builder)
    }

    @Test
    void testAddMetric() {
        delegate.apply {
            metric new CoveragePredictMetric()
            metric new RMSEPredictMetric()
        }
        def metrics = builder.getMetrics()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testAddMetricsByClass() {
        delegate.apply {
            metric CoveragePredictMetric
            metric RMSEPredictMetric
        }
        def metrics = builder.getMetrics()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testSetOutput() {
        delegate.apply {
            output "eval-out.csv"
        }
        assertThat(builder.getOutput(), equalTo(new File("eval-out.csv")))
    }

    @Test
    void testPredictOutput() {
        delegate.apply {
            predictOutput "predictions.csv"
        }
        assertThat(builder.getPredictOutput(), equalTo(new File("predictions.csv")))
    }

    @Test
    void testGenericInput() {
        delegate.apply {
            dataSource {
                train csvfile("train.csv")
                test csvfile("test.csv")
            }
        }
        def data = builder.dataSources()
        assertThat(data.size(), equalTo(1))
        assertThat(data.get(0), instanceOf(GenericTTDataSet))
        GenericTTDataSet ds = data.get(0) as GenericTTDataSet
        assertThat(ds.trainData, instanceOf(CSVDataSource))
        assertThat(ds.trainData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.testData, instanceOf(CSVDataSource))
        assertThat(ds.testData.sourceFile, equalTo(new File("test.csv")))
    }

    @Test
    void testGenericDefaults() {
        assertTrue(true)
        delegate.apply {
            dataSource {
                train "train.csv"
                test "test.csv"
            }
        }
        def data = builder.dataSources()
        assertThat(data.size(), equalTo(1))
        assertThat(data.get(0), instanceOf(GenericTTDataSet))
        GenericTTDataSet ds = data.get(0) as GenericTTDataSet
        assertThat(ds.trainData, instanceOf(CSVDataSource))
        assertThat(ds.trainData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.testData, instanceOf(CSVDataSource))
        assertThat(ds.testData.sourceFile, equalTo(new File("test.csv")))
    }
}
