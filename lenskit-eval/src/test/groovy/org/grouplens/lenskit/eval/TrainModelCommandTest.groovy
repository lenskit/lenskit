package org.grouplens.lenskit.eval

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselineItemScorer
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.GlobalMeanPredictor
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.event.SimpleRating
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.grouplens.lenskit.eval.data.GenericDataSource
import org.grouplens.lenskit.vectors.MutableSparseVector
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TrainModelCommandTest extends ConfigTestBase {
    def ratings = [
            new SimpleRating(1, 1, 1, 3.5),
            new SimpleRating(2, 1, 2, 4.0),
            new SimpleRating(3, 2, 1, 3.5),
            new SimpleRating(3, 2, 3, 5.0)
    ]
    def daoFactory = new EventCollectionDAO.Factory(ratings);
    def dataSource = new GenericDataSource("test-data", daoFactory);

    @Test
    void testTrainModel() {
        def obj = eval {
            trainModel {
                algorithm {
                    bind ItemScorer to BaselineItemScorer
                    bind BaselinePredictor to GlobalMeanPredictor
                }
                input dataSource
                action {
                    assertThat(it.itemScorer, notNullValue());
                    assertThat(it.ratingPredictor, notNullValue());
                    assertThat(it.get(BaselinePredictor), notNullValue());
                    return it.itemScorer.baseline
                }
            }
        }
        assertThat(obj, instanceOf(GlobalMeanPredictor))
        def v = obj.predict(42, new MutableSparseVector(), [1l,2l,4l])
        assertThat(v.get(1), closeTo(4.0d, 1.0e-5d))
        assertThat(v.get(2), closeTo(4.0d, 1.0e-5d))
        assertThat(v.get(4), closeTo(4.0d, 1.0e-5d))
    }
}
