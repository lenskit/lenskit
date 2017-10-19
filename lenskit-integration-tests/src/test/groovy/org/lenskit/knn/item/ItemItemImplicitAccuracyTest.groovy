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
import org.lenskit.LenskitConfiguration
import org.lenskit.api.ItemScorer
import org.lenskit.config.ConfigHelpers
import org.lenskit.data.entities.CommonTypes
import org.lenskit.data.ratings.EntityCountRatingVectorPDAO
import org.lenskit.data.ratings.InteractionEntityType
import org.lenskit.data.ratings.RatingVectorPDAO
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.recommend.RecommendEvalTask
import org.lenskit.eval.traintest.recommend.TopNMAPMetric
import org.lenskit.eval.traintest.recommend.TopNMRRMetric
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
import static org.junit.Assert.assertThat

/**
 * Do major tests on the user-user recommender with implicit feedback.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ItemItemImplicitAccuracyTest extends CrossfoldTestSuite {
    List<Table> runList

    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to ItemItemScorer
            bind NeighborhoodScorer to SimilaritySumNeighborhoodScorer
            bind RatingVectorPDAO to EntityCountRatingVectorPDAO
            set InteractionEntityType to CommonTypes.RATING
            set NeighborhoodSize to 20
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
                    closeTo(0.215d, 0.01d))
            assertThat(table.column("MRR").average(),
                    closeTo(0.59d, 0.05d))
            assertThat(table.column("nDCG").average(),
                    closeTo(0.505d, 0.01d))
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
            def test = new ItemItemImplicitAccuracyTest()
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
