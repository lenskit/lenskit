/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.transform.normalize.BiasUserVectorNormalizer
import org.lenskit.transform.normalize.UserVectorNormalizer
import org.grouplens.lenskit.vectors.similarity.SimilarityDamping
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
