/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.user

import org.grouplens.lenskit.ItemRecommender
import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.RecommenderBuildException
import org.grouplens.lenskit.baseline.BaselineScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.baseline.UserMeanBaseline
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.basic.SimpleRatingPredictor
import org.grouplens.lenskit.basic.TopNItemRecommender
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.core.LenskitRecommender
import org.grouplens.lenskit.core.LenskitRecommenderEngine
import org.grouplens.lenskit.core.ModelDisposition
import org.grouplens.lenskit.test.ML100KTestSuite
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity
import org.junit.Test

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
        assertThat(rec.getItemScorer(),
                   instanceOf(UserUserItemScorer.class))
        ItemRecommender recommender = rec.getItemRecommender()
        assertThat(recommender, instanceOf(TopNItemRecommender.class))
        assertThat(((TopNItemRecommender) recommender).getScorer(),
                   sameInstance(rec.getItemScorer()))
        RatingPredictor pred = rec.getRatingPredictor()
        assertThat(pred, instanceOf(SimpleRatingPredictor.class))
        assertThat(((SimpleRatingPredictor) pred).getScorer(),
                   sameInstance(rec.getItemScorer()))
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
            bind NeighborhoodFinder to SnapshotNeighborhoodFinder
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
        assertThat(rec.getItemScorer(),
                   instanceOf(UserUserItemScorer.class))
        ItemRecommender recommender = rec.getItemRecommender()
        assertThat(recommender, instanceOf(TopNItemRecommender.class))
        assertThat(((TopNItemRecommender) recommender).getScorer(),
                   sameInstance(rec.getItemScorer()))
        RatingPredictor pred = rec.getRatingPredictor()
        assertThat(pred, instanceOf(SimpleRatingPredictor.class))
        assertThat(((SimpleRatingPredictor) pred).getScorer(),
                   sameInstance(rec.getItemScorer()))


        UserUserItemScorer is = rec.itemScorer as UserUserItemScorer
        assertThat is.neighborhoodFinder, instanceOf(SnapshotNeighborhoodFinder)
        def rec2 = loaded.createRecommender()
        assertThat((rec2.itemScorer as UserUserItemScorer).neighborhoodFinder,
                   sameInstance(is.neighborhoodFinder))
    }
}
