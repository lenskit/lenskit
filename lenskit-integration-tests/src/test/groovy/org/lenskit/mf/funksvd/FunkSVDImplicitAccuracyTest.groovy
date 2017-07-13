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
package org.lenskit.mf.funksvd

import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.LenskitConfiguration
import org.lenskit.api.ItemScorer
import org.lenskit.bias.BiasModel
import org.lenskit.bias.ZeroBiasModel
import org.lenskit.config.ConfigHelpers
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.data.ratings.RatingVectorPDAO
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.recommend.RecommendEvalTask
import org.lenskit.eval.traintest.recommend.TopNMAPMetric
import org.lenskit.eval.traintest.recommend.TopNMRRMetric
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
import static org.junit.Assert.assertThat

/**
 * Do major tests on the FunkSVD recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class FunkSVDImplicitAccuracyTest extends CrossfoldTestSuite {
    List<Table> runList = null

    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to FunkSVDItemScorer
            bind BiasModel to ZeroBiasModel
            bind RatingVectorPDAO to EntityCountRatingVectorPDAO
            set InteractionEntityType to CommonTypes.RATING
            set FeatureCount to 25
            set IterationCount to 125
        }
    }

    @Override
    void addExtraConfig(SimpleEvaluator eval) {
        def exp = eval.experiment
        exp.tasks.clear()
        def task = new RecommendEvalTask()
        task.addMetric(new TopNMAPMetric())
        task.addMetric(new TopNMRRMetric())
        exp.addTask(task)
    }

    @Override
    protected void checkResults(Table table) {
        if (runList == null) {
            assertThat(table.column("MAP").average(),
                    closeTo(0.0565d, 0.0025d))
            assertThat(table.column("MRR").average(),
                    closeTo(0.08d, 0.012d))
            assertThat(table.column("nDCG").average(),
                    closeTo(0.303d, 0.01d))
        } else {
            runList << table
        }
    }

    static void main(String[] args) {
        def nruns = 10
        if (args.length > 1) {
            nruns = args[0].toInteger()
        }
        List<Table> list = []
        for (int i = 0; i < nruns; i++) {
            System.out.format("run %d of %d\n", i+1, nruns)
            def test = new FunkSVDImplicitAccuracyTest()
            test.runList = list
            test.workDir.create()
            try {
                test.createDAO()
                test.testAlgorithmAccuracy()
            } finally {
                test.workDir.delete()
            }
        }
        System.out.format("ran %d times\n", nruns)
        def map = list*.column("MAP")*.average()
        def mrr = list*.column("MRR")*.average()
        def ndcg = list*.column("nDCG")*.average()
        System.out.format("MAP: min=%.4f, max=%.4f, mean=%.4f\n",
                          map.min(), map.max(), map.sum() / map.size())
        System.out.format("MRR: min=%.4f, max=%.4f, mean=%.4f\n",
                          mrr.min(), mrr.max(), mrr.sum() / mrr.size())
        System.out.format("nDCG: min=%.4f, max=%.4f, mean=%.4f\n",
                          ndcg.min(), ndcg.max(), ndcg.sum() / ndcg.size())
    }
}
