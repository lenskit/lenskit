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
package org.lenskit.knn.item

import org.grouplens.lenskit.test.ML100KTestSuite
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.transform.normalize.UnitVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer
import org.junit.Test
import org.lenskit.LenskitRecommender
import org.lenskit.LenskitRecommenderEngine
import org.lenskit.api.ItemScorer
import org.lenskit.api.RecommenderBuildException
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.UserMeanBaseline
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.config.ConfigHelpers
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.RatingVectorPDAO
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
            bind ItemScorer to ItemItemScorer
            within (UserVectorNormalizer) {
                bind VectorNormalizer to UnitVectorNormalizer
            }
            bind RatingVectorPDAO to EntityCountRatingVectorPDAO
            set InteractionEntityType to LIKE
            bind (BaselineScorer, ItemScorer) to UserMeanItemScorer
            bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
        }

        def dao = implicitSource.get()

        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .build(dao)
        assertThat(engine, notNullValue())

        LenskitRecommender rec = engine.createRecommender(dao)
        assertThat(rec.itemScorer,
                   instanceOf(ItemItemScorer.class))
        assertThat(rec.get(ItemItemModel.class),
                   notNullValue())

        def recs = rec.itemRecommender.recommend(100, 10)
        assertThat(recs, hasSize(10))
    }
}
