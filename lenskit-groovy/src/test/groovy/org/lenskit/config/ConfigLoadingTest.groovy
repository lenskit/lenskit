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
package org.lenskit.config

import org.junit.Test
import org.lenskit.LenskitConfiguration
import org.lenskit.LenskitRecommenderEngine
import org.lenskit.RecommenderConfigurationException
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.basic.ConstantItemScorer
import org.lenskit.basic.SimpleRatingPredictor
import org.lenskit.basic.TopNItemRecommender
import org.lenskit.bias.BiasModel
import org.lenskit.bias.GlobalBiasModel
import org.lenskit.data.dao.DataAccessObject
import org.lenskit.data.dao.file.StaticDataSource
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer

import static junit.framework.Assert.fail
import static org.grouplens.lenskit.util.test.ExtraMatchers.matchesPattern
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class ConfigLoadingTest {
    StaticDataSource source = StaticDataSource.fromList([])
    DataAccessObject dao = source.get()

    @Test
    void testLoadBasicConfig() {
        LenskitConfiguration config = ConfigHelpers.load {
            bind ItemScorer to ConstantItemScorer
            set ConstantItemScorer.Value to Math.PI
        }
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(ConstantItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            def bl = rec.itemScorer as ConstantItemScorer
            assertThat(bl.fixedScore, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testEditBasicConfig() {
        LenskitConfiguration config = new LenskitConfiguration()
        config.set(ConstantItemScorer.Value).to(Math.PI)
        ConfigHelpers.configure(config) {
            bind ItemScorer to ConstantItemScorer
        }
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(ConstantItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            def bl = rec.itemScorer as ConstantItemScorer
            assertThat(bl.fixedScore, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadNewRoot() {
        LenskitConfiguration config = ConfigHelpers.load {
            root UserVectorNormalizer
            bind UserVectorNormalizer to BiasUserVectorNormalizer
            bind BiasModel to GlobalBiasModel
        }
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), nullValue());
            assertThat(rec.getItemRecommender(), nullValue())
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            assertThat(rec.get(UserVectorNormalizer),
                       instanceOf(BiasUserVectorNormalizer))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadWithinBlock() {
        LenskitConfiguration config = ConfigHelpers.load {
            root UserVectorNormalizer
            bind UserVectorNormalizer to BiasUserVectorNormalizer
            within (UserVectorNormalizer) {
                bind BiasModel to GlobalBiasModel
            }
        }
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), nullValue());
            assertThat(rec.getItemRecommender(), nullValue())
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            def sim = rec.get(UserVectorNormalizer)
            assertThat(sim,
                       instanceOf(BiasUserVectorNormalizer))
            assertThat(sim.model, instanceOf(GlobalBiasModel))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadBasicText() {
        LenskitConfiguration config = ConfigHelpers.load(
                """import org.lenskit.baseline.*
bind ItemScorer to ConstantItemScorer
set ConstantItemScorer.Value to Math.PI""");
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(ConstantItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            assertThat(rec.itemScorer.fixedScore, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadBasicURL() {
        LenskitConfiguration config = ConfigHelpers.load(getClass().getResource('test-config.groovy'))
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        assertThat(rec.getItemScorer(), instanceOf(ConstantItemScorer))
        assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
        assertThat(rec.getItemBasedItemRecommender(), nullValue());
        assertThat(rec.itemScorer.fixedScore, equalTo(Math.PI))
    }

    @Test
    void testLoadURLWithInclude() {
        LenskitConfiguration config = ConfigHelpers.load(getClass().getResource('test-include.groovy'))
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(ConstantItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            assertThat(rec.itemScorer.fixedScore, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testModifyBasicText() {
        LenskitConfiguration config = new LenskitConfiguration()
        ConfigurationLoader loader = new ConfigurationLoader();
        LenskitConfigScript script = loader.loadScript(
                """
bind ItemScorer to ConstantItemScorer
set ConstantItemScorer.Value to Math.PI""");
        script.configure(config);
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(ConstantItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getItemBasedItemRecommender(), nullValue());
            assertThat(rec.itemScorer.fixedScore, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testPreferenceDomain() {
        LenskitConfiguration config = ConfigHelpers.load {
            bind ItemScorer to ItemMeanRatingItemScorer
            domain minimum: 1, maximum: 5, precision: 0.5
        }
        def engine = LenskitRecommenderEngine.build(config, dao)
        def rec = engine.createRecommender(dao)
        try {
            assertThat(rec.getItemScorer(), instanceOf(ItemMeanRatingItemScorer));
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            def rp = rec.getRatingPredictor()
            assertThat(rp, anyOf(instanceOf(SimpleRatingPredictor)))
            assertThat((rp as SimpleRatingPredictor).scorer,
                       sameInstance(rec.getItemScorer()))
            def dom = (rp as SimpleRatingPredictor).preferenceDomain
            assertThat(dom, notNullValue());
            assertThat(dom.minimum, equalTo(1.0d))
            assertThat(dom.maximum, equalTo(5.0d))
            assertThat(dom.precision, equalTo(0.5d))
        } finally {
            rec.close()
        }
    }

    @Test
    public void testMissingClassErrors() {
        ConfigurationLoader loader = new ConfigurationLoader()
        LenskitConfigScript script = loader.loadScript(
                """
root PreferenceDomain
""")
        def config
        try {
            config = script.configure()
            fail("script load should fail")
        } catch (RecommenderConfigurationException rce) {
            assertThat(rce.hints, contains(matchesPattern(/.*import.*\s+org\.lenskit\.data\.ratings\.PreferenceDomain/)))
        }
    }
}
