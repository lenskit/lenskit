/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.predict

import org.lenskit.basic.RescoringItemRecommender
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.data.event.Rating
import org.junit.Ignore
import org.junit.Test
import org.lenskit.LenskitRecommender
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.basic.SimpleRatingPredictor
import org.lenskit.basic.TopNItemRecommender

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

class RatingPredictorItemScorerTest {
    @Test
    void testSophisticatedConfig() {
        def dao = EventCollectionDAO.create([
                Rating.create(1, 11, 3.0),
                Rating.create(1, 12, 5.0),
                Rating.create(2, 12, 3.5),
                Rating.create(2, 11, 3.0)
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
    }
}
