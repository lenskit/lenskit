package org.grouplens.lenskit.eval.traintest

import org.grouplens.lenskit.eval.config.BuilderDelegate
import org.grouplens.lenskit.eval.config.EvalConfigEngine
import org.grouplens.lenskit.eval.data.CSVDataSource
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.junit.Before
import org.junit.Test
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

/**
 * Tests for train-test configurations; they also serve to test the builder delegate
 * framework.
 * @author Michael Ekstrand
 */
class TestTrainTestBuilderConfig {
    EvalConfigEngine engine
    TrainTestEvalBuilder builder
    BuilderDelegate delegate

    @Before
    void setupDelegate() {
        engine = new EvalConfigEngine()
        builder = new TrainTestEvalBuilder()
        delegate = new BuilderDelegate(engine, builder)
    }
    
    def eval(Closure cl) {
        cl.setDelegate(delegate)
        cl.setResolveStrategy(Closure.DELEGATE_FIRST)
        cl.call()
    }

    @Test
    void testAddMetric() {
        eval {
            metric new CoveragePredictMetric()
            metric new RMSEPredictMetric()
        }
        def metrics = builder.getMetrics()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testAddMetricsByClass() {
        eval {
            metric CoveragePredictMetric
            metric RMSEPredictMetric
        }
        def metrics = builder.getMetrics()
        assertThat(metrics.size(), equalTo(2))
    }

    @Test
    void testSetOutput() {
        eval {
            output "eval-out.csv"
        }
        assertThat(builder.getOutput(), equalTo(new File("eval-out.csv")))
    }

    @Test
    void testPredictOutput() {
        eval {
            predictOutput "predictions.csv"
        }
        assertThat(builder.getPredictOutput(), equalTo(new File("predictions.csv")))
    }

    @Test
    void testGenericInput() {
        boolean closureInvoked = false
        eval {
            dataset {
                // check some things about our strategy...
                assertThat(delegate, instanceOf(BuilderDelegate))
                assertThat(resolveStrategy, equalTo(Closure.DELEGATE_FIRST))
                assertThat(delegate.builder, instanceOf(GenericTTDataBuilder))

                closureInvoked = true

                train csvfile("train.csv") {
                    delimiter ","
                }
                test csvfile("test.csv")
            }
        }
        assertTrue(closureInvoked)
        def data = builder.dataSources()
        assertThat(data.size(), equalTo(1))
        assertThat(data.get(0), instanceOf(GenericTTDataSet))
        GenericTTDataSet ds = data.get(0) as GenericTTDataSet
        assertThat(ds.trainData, instanceOf(CSVDataSource))
        assertThat(ds.trainData.sourceFile, equalTo(new File("train.csv")))
        assertThat(ds.trainData.delimiter, equalTo(","))
        assertThat(ds.testData, instanceOf(CSVDataSource))
        assertThat(ds.testData.sourceFile, equalTo(new File("test.csv")))
    }

    @Test
    void testGenericDefaults() {
        boolean closureInvoked = false
        eval {
            dataset {
                closureInvoked = true
                train "train.csv"
                test "test.csv"
            }
        }
        assertTrue(closureInvoked)
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
    void testCrossfoldDataSource() {
        eval {
            dataset crossfold("ml-100k") {
                source "ml-100k.csv"
                partitions 7
            }
        }
        def data = builder.dataSources()
        assertThat(data.size(), equalTo(7))
    }
}
