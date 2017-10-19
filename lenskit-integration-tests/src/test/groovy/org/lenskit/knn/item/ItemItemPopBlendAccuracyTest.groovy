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

import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.lenskit.similarity.SimilarityDamping
import org.lenskit.LenskitConfiguration
import org.lenskit.api.ItemRecommender
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.BaselineScorer
import org.lenskit.basic.PopularityRankItemScorer
import org.lenskit.bias.BiasItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.config.ConfigHelpers
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.recommend.ItemSelector
import org.lenskit.eval.traintest.recommend.RecommendEvalTask
import org.lenskit.eval.traintest.recommend.TopNMAPMetric
import org.lenskit.eval.traintest.recommend.TopNMRRMetric
import org.lenskit.hybrid.BlendWeight
import org.lenskit.hybrid.RankBlendingItemRecommender
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
import static org.junit.Assert.assertThat

/**
 * Do major tests on the item-item recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemItemPopBlendAccuracyTest extends CrossfoldTestSuite {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to ItemItemScorer
            bind (BaselineScorer, ItemScorer) to BiasItemScorer
            bind BiasModel to UserItemBiasModel
            bind UserVectorNormalizer to BiasUserVectorNormalizer
            within (ItemSimilarity) {
                set SimilarityDamping to 100.0
            }
            set NeighborhoodSize to 30

            // add 10% popularity rank
            bind ItemRecommender to RankBlendingItemRecommender
            within (RankBlendingItemRecommender.Right, ItemRecommender) {
                bind ItemScorer to PopularityRankItemScorer
            }
            set BlendWeight to 0.9
        }
    }

    @Override
    void addExtraConfig(SimpleEvaluator eval) {
        def task = new RecommendEvalTask()
        task.candidateSelector = ItemSelector.allItems()
        task.excludeSelector = ItemSelector.userTestItems()
        task.addMetric(new TopNMAPMetric())
        task.addMetric(new TopNMRRMetric())
        eval.experiment.addTask(task)
    }

    @Override
    protected void checkResults(Table table) {
        // same accuracy as the main item-item
        assertThat(table.column("MAE.ByRating").average(),
                   closeTo(0.70d, 0.025d))
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.90d, 0.05d))
        // and test that MAP!
        // FIXME Establish MAP/MRR test values
        // assertThat(table.column("MAP").average(),
                   // closeTo(0.2, 0.05d))
    }
}
