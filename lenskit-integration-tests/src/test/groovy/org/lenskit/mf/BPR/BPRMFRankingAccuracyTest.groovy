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
package org.lenskit.mf.BPR

import org.grouplens.lenskit.iterative.IterationCount
import org.grouplens.lenskit.test.CrossfoldTestSuite
import org.lenskit.LenskitConfiguration
import org.lenskit.api.ItemScorer
import org.lenskit.bias.BiasDamping
import org.lenskit.bias.BiasModel
import org.lenskit.bias.UserItemBiasModel
import org.lenskit.bias.ZeroBiasModel
import org.lenskit.config.ConfigHelpers
import org.lenskit.eval.traintest.SimpleEvaluator
import org.lenskit.eval.traintest.predict.NDCGPredictMetric
import org.lenskit.eval.traintest.recommend.ItemSelector
import org.lenskit.eval.traintest.recommend.RecommendEvalTask
import org.lenskit.eval.traintest.recommend.TopNMRRMetric
import org.lenskit.mf.funksvd.FeatureCount
import org.lenskit.mf.funksvd.FunkSVDItemScorer
import org.lenskit.mf.svd.BiasedMFItemScorer
import org.lenskit.mf.svd.MFModel
import org.lenskit.util.table.Table

import static org.hamcrest.Matchers.closeTo
import static org.junit.Assert.assertThat

/**
 * Do major tests on the FunkSVD recommender.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BPRMFRankingAccuracyTest extends CrossfoldTestSuite {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to BiasedMFItemScorer
            bind BiasModel to ZeroBiasModel
            bind MFModel toProvider BPRMFModelProvider
            bind TrainingPairGenerator to RandomRatingPairGenerator

            set FeatureCount to 25
            set IterationCount to 125
            set BatchSize to 2000
        }
    }

    @Override
    void addExtraConfig(SimpleEvaluator eval) {
        eval.addMetric(new NDCGPredictMetric())
    }

    @Override
    protected void checkResults(Table table) {
        assertThat(table.column("Predict.nDCG").average(),
                   closeTo(0.914d, 0.025d))
    }
}
