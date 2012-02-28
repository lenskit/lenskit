import org.grouplens.lenskit.baseline.GlobalMeanPredictor
import org.grouplens.lenskit.baseline.UserMeanPredictor
import org.grouplens.lenskit.baseline.ItemMeanPredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric

def buildDir = System.getProperty("project.build.directory", ".")

def baselines = [GlobalMeanPredictor, UserMeanPredictor, ItemMeanPredictor, ItemUserMeanPredictor]

trainTest {
    output "${buildDir}/eval-output/baselines.csv"
    dataset crossfold {
        source csvfile("ml-100k") {
            file "${buildDir}/ml-100k/u.data"
            delimiter "\t"
            domain {
                minimum 1.0
                maximum 5.0
                precision 1.0
            }
        }
    }

    metric CoveragePredictMetric
    metric MAEPredictMetric
    metric RMSEPredictMetric

    for (bl in baselines) {
        algorithm(bl.simpleName) {
            setComponent(RatingPredictor, BaselineRatingPredictor)
            setComponent(BaselinePredictor, bl)
        }
    }
}
