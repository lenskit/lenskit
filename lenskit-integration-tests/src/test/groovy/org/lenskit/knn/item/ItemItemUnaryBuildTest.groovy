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
package org.lenskit.knn.item

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.RecommenderBuildException
import org.grouplens.lenskit.baseline.BaselineScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.baseline.UserMeanBaseline
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.core.LenskitRecommender
import org.grouplens.lenskit.core.LenskitRecommenderEngine
import org.grouplens.lenskit.data.event.EventType
import org.grouplens.lenskit.data.event.Like
import org.grouplens.lenskit.data.history.EventCountUserHistorySummarizer
import org.grouplens.lenskit.data.history.UserHistorySummarizer
import org.grouplens.lenskit.test.ML100KTestSuite
import org.grouplens.lenskit.transform.normalize.UnitVectorNormalizer
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer
import org.junit.Test
import org.lenskit.knn.item.model.ItemItemModel

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * Test whether we can build an item-item model over unary data.
 */
public class ItemItemUnaryBuildTest extends ML100KTestSuite {
    @Test
    public void testBuildImplicitModelModel() throws RecommenderBuildException, IOException {
        def config = ConfigHelpers.load {
            addComponent implicitDAO
            bind ItemScorer to ItemItemScorer
            within (UserVectorNormalizer) {
                bind VectorNormalizer to UnitVectorNormalizer
            }
            bind UserHistorySummarizer to EventCountUserHistorySummarizer
            set EventType toInstance Like
            bind (BaselineScorer, ItemScorer) to UserMeanItemScorer
            bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
        }

        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .build()
        assertThat(engine, notNullValue())

        LenskitRecommender rec = engine.createRecommender()
        assertThat(rec.itemScorer,
                   instanceOf(ItemItemScorer.class))
        assertThat(rec.get(ItemItemModel.class),
                   notNullValue())

        def recs = rec.itemRecommender.recommend(100, 10)
        assertThat(recs, hasSize(10))
    }
}
