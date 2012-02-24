package org.grouplens.lenskit.eval

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.junit.Test
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.BaselineRatingPredictor
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.GlobalMeanPredictor

/**
 * @author Michael Ekstrand
 */
class TestAlgorithmInstanceConfig extends ConfigTestBase {
    @Test
    void testBasicAlgorithm() {
        def obj = eval {
            algorithm("GlobalMean") {
                setComponent(RatingPredictor, BaselineRatingPredictor)
                setComponent(BaselinePredictor, GlobalMeanPredictor)
                wombat = "global"
            }
        }
        assertThat(obj, instanceOf(AlgorithmInstance))
        def algo = obj as AlgorithmInstance
        assertThat(algo.name, equalTo("GlobalMean"))
        assertThat(algo.attributes["wombat"] as String, equalTo("global"))
    }
}
