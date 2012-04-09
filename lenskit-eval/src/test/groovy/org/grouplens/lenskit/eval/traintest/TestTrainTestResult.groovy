package org.grouplens.lenskit.eval.traintest

import org.junit.Before
import org.grouplens.lenskit.eval.config.EvalConfigEngine
import org.grouplens.lenskit.eval.config.CommandDelegate
import org.junit.Test
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.eval.results.TrainTestEvalResult
import org.grouplens.lenskit.eval.results.ResultRow
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import org.grouplens.lenskit.eval.config.CommandDelegate

/**
 * Test the result returned by the trainTest
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 *
 */
class TestTrainTestResult extends ConfigTestBase{
    def file = new File("ml-100k.csv")

    @Before
    void prepareFile() {
        file.append('19,242,3,881250949\n')
        file.append('296,242,3.5,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
        file.append('196,242,3,881250949\n')
    }

    @Test
    void TestResult() {
        def dat = eval{
            crossfold("ml-100k") {
                source file
                partitions 5
            }
        }
        def result = eval{
            trainTest {
                dataset dat

                metric MAEPredictMetric
                metric RMSEPredictMetric

                algorithm("ItemUserMean") {
                    setComponent(RatingPredictor, BaselineRatingPredictor)
                    setComponent(BaselinePredictor, ItemUserMeanPredictor)
                }
            }
        }

        assertThat(result, instanceOf(TrainTestEvalResult))
        assertThat(result.getPartition(), equalTo(5))
        assertThat(result.getField(0), equalTo("BuildTime"))
        assertThat(result.getField(1), equalTo("TestTime"))
        assertThat(result.getField(2), equalTo("MAE"))
        assertThat(result.getRow("ItemUserMean", 0), instanceOf(ResultRow))


    }
}
