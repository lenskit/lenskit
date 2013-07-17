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
package org.grouplens.lenskit.config

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselineItemScorer
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.ConstantPredictor
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor
import org.grouplens.lenskit.basic.SimpleRatingPredictor
import org.grouplens.lenskit.basic.TopNItemRecommender
import org.grouplens.lenskit.core.LenskitConfiguration
import org.grouplens.lenskit.core.LenskitRecommenderEngine

import org.grouplens.lenskit.data.dao.EventCollectionDAO
import org.grouplens.lenskit.vectors.similarity.PearsonCorrelation
import org.grouplens.lenskit.vectors.similarity.SignificanceWeightedVectorSimilarity
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class TestConfigLoading {
    DAOFactory factory = new EventCollectionDAO.Factory([])

    @Test
    void testLoadBasicConfig() {
        LenskitConfiguration config = ConfigHelpers.load {
            bind BaselinePredictor to ConstantPredictor
            bind ItemScorer to BaselineItemScorer
            set ConstantPredictor.Value to Math.PI
        }
        def engine = LenskitRecommenderEngine.build(factory, config)
        def rec = engine.open()
        try {
            assertThat(rec.getItemScorer(), instanceOf(BaselineItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getGlobalItemRecommender(), nullValue());
            def bl = rec.get(BaselinePredictor)
            assertThat(bl, instanceOf(ConstantPredictor))
            assertThat(bl.value, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadNewRoot() {
        LenskitConfiguration config = ConfigHelpers.load {
            root VectorSimilarity
            bind VectorSimilarity to PearsonCorrelation
        }
        def engine = LenskitRecommenderEngine.build(factory, config)
        def rec = engine.open()
        try {
            assertThat(rec.getItemScorer(), nullValue());
            assertThat(rec.getItemRecommender(), nullValue())
            assertThat(rec.getGlobalItemRecommender(), nullValue());
            assertThat(rec.get(VectorSimilarity),
                       instanceOf(PearsonCorrelation))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadWithinBlock() {
        LenskitConfiguration config = ConfigHelpers.load {
            root VectorSimilarity
            bind VectorSimilarity to SignificanceWeightedVectorSimilarity
            within(VectorSimilarity) {
                bind VectorSimilarity to PearsonCorrelation
            }
        }
        def engine = LenskitRecommenderEngine.build(factory, config)
        def rec = engine.open()
        try {
            assertThat(rec.getItemScorer(), nullValue());
            assertThat(rec.getItemRecommender(), nullValue())
            assertThat(rec.getGlobalItemRecommender(), nullValue());
            def sim = rec.get(VectorSimilarity)
            assertThat(sim,
                       instanceOf(SignificanceWeightedVectorSimilarity))
            assertThat(sim.delegate, instanceOf(PearsonCorrelation))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadBasicText() {
        LenskitConfiguration config = ConfigHelpers.load(
                """import org.grouplens.lenskit.baseline.*
bind BaselinePredictor to ConstantPredictor
bind ItemScorer to BaselineItemScorer
set ConstantPredictor.Value to Math.PI""");
        def engine = LenskitRecommenderEngine.build(factory, config)
        def rec = engine.open()
        try {
            assertThat(rec.getItemScorer(), instanceOf(BaselineItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getGlobalItemRecommender(), nullValue());
            def bl = rec.get(BaselinePredictor)
            assertThat(bl, instanceOf(ConstantPredictor))
            assertThat(bl.value, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testLoadBasicURL() {
        LenskitConfiguration config = ConfigHelpers.load(getClass().getResource("test-config.groovy"))
        def engine = LenskitRecommenderEngine.build(factory, config)
        def rec = engine.open()
        try {
            assertThat(rec.getItemScorer(), instanceOf(BaselineItemScorer))
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            assertThat(rec.getGlobalItemRecommender(), nullValue());
            def bl = rec.get(BaselinePredictor)
            assertThat(bl, instanceOf(ConstantPredictor))
            assertThat(bl.value, equalTo(Math.PI))
        } finally {
            rec.close()
        }
    }

    @Test
    void testPreferenceDomain() {
        LenskitConfiguration config = ConfigHelpers.load {
            bind BaselinePredictor to ItemUserMeanPredictor
            bind ItemScorer to BaselineItemScorer
            domain minimum: 1, maximum: 5, precision: 0.5
        }
        def engine = LenskitRecommenderEngine.build(factory, config)
        def rec = engine.open()
        try {
            assertThat(rec.getItemScorer(), instanceOf(BaselineItemScorer));
            assertThat(rec.getItemRecommender(), instanceOf(TopNItemRecommender))
            def rp = rec.getRatingPredictor()
            assertThat(rp, instanceOf(SimpleRatingPredictor))
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
}
