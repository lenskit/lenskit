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
package org.grouplens.lenskit.mf.funksvd

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.RecommenderBuildException
import org.grouplens.lenskit.baseline.*
import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.core.LenskitRecommender
import org.grouplens.lenskit.core.LenskitRecommenderEngine
import org.grouplens.lenskit.core.ModelDisposition
import org.grouplens.lenskit.data.dao.EventDAO
import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.test.ML100KTestSuite
import org.junit.Test

import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

/**
 * Do major tests on the FunkSVD recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FunkSVDBuildSerializeTest extends ML100KTestSuite {
    def config = ConfigHelpers.load {
        bind ItemScorer to FunkSVDItemScorer
        bind (BaselineScorer, ItemScorer) to UserMeanItemScorer
        bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
        set FeatureCount to 10
        set IterationCount to 10
        within (BaselineScorer, ItemScorer) {
            set MeanDamping to 25
        }
    }

    @Test
    void testBuildWithMissingItems() {
        LenskitRecommenderEngine engine =
                LenskitRecommenderEngine.newBuilder()
                                        .addConfiguration(config)
                                        .addConfiguration(itemSubsetConfig, ModelDisposition.EXCLUDED)
                                        .build()
        assertThat(engine, notNullValue())
    }

    @Test
    void testBuildAndSerializeModel() throws RecommenderBuildException, IOException {
        LenskitRecommenderEngine engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(daoConfig, ModelDisposition.EXCLUDED)
                                    .build()
        assertThat(engine, notNullValue())

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        engine.write(out)
        byte[] bytes = out.toByteArray()

        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        LenskitRecommenderEngine loaded =
            LenskitRecommenderEngine.newLoader()
                                    .addConfiguration(daoConfig)
                                    .load(input)

        assertThat(loaded, notNullValue())

        LenskitRecommender rec = loaded.createRecommender()
        assertThat(rec.getItemScorer(),
                   instanceOf(FunkSVDItemScorer))
        assertThat(rec.get(FunkSVDModel),
                   notNullValue())
    }
}
