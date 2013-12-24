package org.grouplens.lenskit.predict

import org.grouplens.lenskit.ItemRecommender
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.basic.RescoringItemRecommender
import org.grouplens.lenskit.basic.SimpleRatingPredictor
import org.grouplens.lenskit.basic.TopNItemRecommender
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.core.LenskitRecommender
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.event.Ratings
import org.grouplens.lenskit.transform.quantize.QuantizedRatingPredictor;
import org.junit.Test

import static org.hamcrest.Matchers.closeTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat;

class RatingPredictorItemScorerTest {
    @Test
    void testSophisticatedConfig() {
        def dao = EventCollectionDAO.create([
                Ratings.make(1, 11, 3.0),
                Ratings.make(1, 12, 5.0),
                Ratings.make(2, 12, 3.5),
                Ratings.make(2, 11, 3.0)
        ])
        def config = ConfigHelpers.load {
            domain minimum: 1.0, maximum: 5.0, precision: 1.0
            bind EventDAO to dao
            bind ItemScorer to ItemMeanRatingItemScorer
            bind RatingPredictor to QuantizedRatingPredictor
            bind ItemRecommender to RescoringItemRecommender
            at (RescoringItemRecommender) {
                bind ItemRecommender to TopNItemRecommender
                bind ItemScorer to RatingPredictorItemScorer
            }
            at (QuantizedRatingPredictor) {
                bind RatingPredictor to SimpleRatingPredictor
            }
        }
        def rec = LenskitRecommender.build config
        assertThat(rec.itemRecommender,
                   instanceOf(RescoringItemRecommender))
        assertThat(rec.itemScorer, instanceOf(ItemMeanRatingItemScorer))
        assertThat(rec.ratingPredictor, instanceOf(QuantizedRatingPredictor))

        def irec = rec.itemRecommender
        def recs = irec.recommend(3)
        assertThat(recs, hasSize(2))
        for (sid in recs) {
            if (sid.id == 11) {
                assertThat(sid.score, closeTo(3.0d, 1.0e-6d))
            } else if (sid.id == 12) {
                assertThat(sid.score, closeTo(4.0d, 1.0e-6d))
            } else {
                fail "unexpected item $sid.id"
            }
        }
    }
}
