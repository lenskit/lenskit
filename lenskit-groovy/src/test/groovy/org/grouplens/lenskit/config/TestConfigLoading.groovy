package org.grouplens.lenskit.config

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselineItemScorer
import org.grouplens.lenskit.baseline.BaselinePredictor
import org.grouplens.lenskit.baseline.ConstantPredictor
import org.grouplens.lenskit.basic.TopNItemRecommender
import org.grouplens.lenskit.core.LenskitConfiguration
import org.grouplens.lenskit.core.LenskitRecommenderEngine
import org.grouplens.lenskit.data.dao.DAOFactory
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
}
