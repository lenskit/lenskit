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
package org.lenskit.basic

import org.lenskit.LenskitConfiguration
import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.MeanDamping
import org.lenskit.baseline.UserMeanBaseline
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.config.ConfigHelpers
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.recommend.*
import org.lenskit.util.table.Table

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber
import static org.hamcrest.Matchers.closeTo
import static org.hamcrest.Matchers.not
import static org.junit.Assert.assertThat

/**
 * Do major eval tests.  This runs baseline evaluators, but tests extrta metrics.
 */
public class BasicEvalTest extends CrossfoldTestSuite {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to UserMeanItemScorer
            bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
            set MeanDamping to 5
        }
    }

    @Override
    void addExtraConfig(SimpleEvaluator eval) {
        eval.userOutput = workDir.root.toPath().resolve('user-out.csv')
        RecommendEvalTask task = new RecommendEvalTask()
        task.listSize = 25
        task.labelPrefix = 'All'
        task.addMetric(new TopNMRRMetric())
        task.addMetric(new TopNMAPMetric())
        task.addMetric(new TopNPrecisionRecallMetric())
        task.addMetric(new TopNEntropyMetric())
        task.addMetric(new TopNPopularityMetric())
        task.candidateSelector = ItemSelector.allItems()
        eval.experiment.addTask(task)

        task = new RecommendEvalTask();
        task.listSize = 10
        task.labelPrefix = 'PlusRand'
        task.addMetric(new TopNMRRMetric())
        task.addMetric(new TopNMAPMetric())
        task.addMetric(new TopNPrecisionRecallMetric())
        task.addMetric(new TopNEntropyMetric())
        task.addMetric(new TopNPopularityMetric())
        task.candidateSelector = ItemSelector.compileSelector('pickRandom(getUnseenItems(user), 50)')
        eval.experiment.addTask(task)
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("MAE.ByRating").average(),
                   closeTo(0.75d, 0.025d))
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.93d, 0.05d))
        assertThat(table.column("PlusRand.MAP").average(),
                   not(notANumber()))
    }
}
