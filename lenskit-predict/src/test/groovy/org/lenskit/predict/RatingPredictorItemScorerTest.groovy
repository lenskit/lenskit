/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.predict

import org.junit.Test
import org.lenskit.LenskitRecommender
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.basic.RescoringItemRecommender
import org.lenskit.basic.SimpleRatingPredictor
import org.lenskit.basic.TopNItemRecommender
import org.lenskit.config.ConfigHelpers
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.data.ratings.Rating

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

class RatingPredictorItemScorerTest {
    @Test
    void testSophisticatedConfig() {
        def source = StaticDataSource.fromList([
                Rating.create(1, 11, 3.0),
                Rating.create(1, 12, 5.0),
                Rating.create(2, 12, 3.5),
                Rating.create(2, 11, 3.0)
        ])
        def dao = source.get()
        def config = ConfigHelpers.load {
            domain minimum: 1.0, maximum: 5.0, precision: 1.0
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
        def rec = LenskitRecommender.build config, dao
        try {
            assertThat(rec.itemRecommender,
                       instanceOf(RescoringItemRecommender))
            assertThat(rec.itemScorer, instanceOf(ItemMeanRatingItemScorer))
            assertThat(rec.ratingPredictor, instanceOf(QuantizedRatingPredictor))

            def irec = rec.itemRecommender
            def recs = irec.recommendWithDetails(3, -1, null, null)
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
        } finally {
            rec.close()
        }
    }
}
