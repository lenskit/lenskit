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
package org.lenskit.mf.funksvd

import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.test.ML100KTestSuite
import org.junit.Test
import org.lenskit.LenskitRecommender
import org.lenskit.LenskitRecommenderEngine
import org.lenskit.ModelDisposition
import org.lenskit.api.ItemScorer
import org.lenskit.api.RecommenderBuildException
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.MeanDamping
import org.lenskit.bias.BiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.config.ConfigHelpers

import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat

/**
 * Do major infrastructure tests on the FunkSVD recommender.
 */
public class FunkSVDBuildSerializeTest extends ML100KTestSuite {
    def config = ConfigHelpers.load {
        bind ItemScorer to FunkSVDItemScorer
        bind BiasModel to UserItemBiasModel
        set FeatureCount to 10
        set IterationCount to 10
        within (BaselineScorer, ItemScorer) {
            set MeanDamping to 25
        }
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
        try {
            assertThat(rec.getItemScorer(),
                       instanceOf(FunkSVDItemScorer))
            assertThat(rec.get(FunkSVDModel),
                       notNullValue())
        } finally {
            rec.close()
        }
    }
}
