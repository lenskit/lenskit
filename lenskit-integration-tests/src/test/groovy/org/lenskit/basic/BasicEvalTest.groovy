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
package org.lenskit.basic

import org.grouplens.lenskit.config.ConfigHelpers
import org.grouplens.lenskit.core.LenskitConfiguration
import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.api.ItemScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.baseline.MeanDamping
import org.lenskit.baseline.UserMeanBaseline
import org.lenskit.baseline.UserMeanItemScorer
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.recommend.*
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
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
        RecommendEvalTask task = new RecommendEvalTask()
        task.listSize = 25
        task.addMetric(new TopNMRRMetric())
        task.addMetric(new TopNMAPMetric())
        task.addMetric(new TopNPrecisionRecallMetric())
        task.addMetric(new TopNEntropyMetric())
        task.addMetric(new TopNPopularityMetric())
        eval.experiment.addTask(task)
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("MAE.ByRating").average(),
                   closeTo(0.75d, 0.025d))
        assertThat(table.column("RMSE.ByUser").average(),
                   closeTo(0.93d, 0.05d))
    }
}
