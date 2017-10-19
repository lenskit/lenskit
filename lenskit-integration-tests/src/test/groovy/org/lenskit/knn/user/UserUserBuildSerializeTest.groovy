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
package org.lenskit.knn.user

import org.grouplens.lenskit.test.ML100KTestSuite
import org.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.transform.normalize.VectorNormalizer
import org.lenskit.similarity.CosineVectorSimilarity
import org.lenskit.similarity.VectorSimilarity
import org.junit.Test
import org.lenskit.LenskitRecommender
import org.lenskit.LenskitRecommenderEngine
import org.lenskit.ModelDisposition
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.api.RecommenderBuildException
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.UserMeanBaseline
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.basic.TopNItemRecommender
import org.lenskit.config.ConfigHelpers

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

/**
 * Do major tests on the user-user recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class UserUserBuildSerializeTest extends ML100KTestSuite {

    @Test
    public void testBuildAndSerializeModel() throws RecommenderBuildException, IOException {
        def config = ConfigHelpers.load {
            bind ItemScorer to UserUserItemScorer
            within (UserVectorSimilarity) {
                bind VectorSimilarity to CosineVectorSimilarity
            }
            bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
            bind (BaselineScorer,ItemScorer) to UserMeanItemScorer
            bind (UserMeanBaseline, ItemMeanRatingItemScorer) to ItemMeanRatingItemScorer
        }

        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                    .build()
        assertThat(engine, notNullValue())

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        engine.write(out)
        byte[] bytes = out.toByteArray()

        ByteArrayInputStream input = new ByteArrayInputStream(bytes)
        LenskitRecommenderEngine loaded =
            LenskitRecommenderEngine.newLoader()
                                    .addConfiguration(daoConfig)
                                    .load(input)
        assertThat(loaded, notNullValue())

        LenskitRecommender rec = loaded.createRecommender()
        try {
            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserItemScorer.class))
            ItemRecommender recommender = rec.getItemRecommender()
            assertThat(recommender, instanceOf(TopNItemRecommender.class))
            assertThat(((TopNItemRecommender) recommender).getScorer(),
                       sameInstance(rec.getItemScorer()))
            RatingPredictor pred = rec.getRatingPredictor()
            /* FIXME Re-enable this logic
        assertThat(rec.getRatingPredictor(),
                   instanceOf(SimpleRatingPredictor))
        assertThat(((SimpleRatingPredictor) pred).getScorer(),
                   sameInstance(rec.getItemScorer()))
                   */
        } finally {
            rec.close()
        }
    }

    @Test
    public void testBuildWithPackedRatings() throws RecommenderBuildException, IOException {
        def config = ConfigHelpers.load {
            bind ItemScorer to UserUserItemScorer
            within (UserVectorSimilarity) {
                bind VectorSimilarity to CosineVectorSimilarity
            }
            within (UserVectorNormalizer) {
                bind VectorNormalizer to MeanCenteringVectorNormalizer
            }
            bind (BaselineScorer,ItemScorer) to UserMeanItemScorer
            bind (UserMeanBaseline, ItemMeanRatingItemScorer) to ItemMeanRatingItemScorer
            bind NeighborFinder to SnapshotNeighborFinder
        }

        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                    .build()
        assertThat(engine, notNullValue())

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        engine.write(out)
        byte[] bytes = out.toByteArray()

        ByteArrayInputStream input = new ByteArrayInputStream(bytes)
        LenskitRecommenderEngine loaded =
            LenskitRecommenderEngine.newLoader()
                                    .addConfiguration(daoConfig)
                                    .load(input)
        assertThat(loaded, notNullValue())

        LenskitRecommender rec = loaded.createRecommender()
        try {
            assertThat(rec.getItemScorer(),
                       instanceOf(UserUserItemScorer.class))
            ItemRecommender recommender = rec.getItemRecommender()
            assertThat(recommender, instanceOf(TopNItemRecommender.class))
            assertThat(((TopNItemRecommender) recommender).getScorer(),
                       sameInstance(rec.getItemScorer()))

            /* FIXME re-enable this logic
        assertThat(rec.getRatingPredictor(),
                   instanceOf(SimpleRatingPredictor))
        assertThat(pred, instanceOf(SimpleRatingPredictor.class))
        assertThat(((SimpleRatingPredictor) pred).getScorer(),
                   sameInstance(rec.getItemScorer()))
        */

            UserUserItemScorer is = rec.itemScorer as UserUserItemScorer
            assertThat is.neighborFinder, instanceOf(SnapshotNeighborFinder)
            def rec2 = loaded.createRecommender()
            assertThat((rec2.itemScorer as UserUserItemScorer).neighborFinder.snapshot,
                       sameInstance(is.neighborFinder.snapshot))
        } finally {
            rec.close()
        }
    }
}
