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

import org.grouplens.lenskit.ItemScorer
import org.grouplens.lenskit.baseline.BaselineScorer
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer
import org.grouplens.lenskit.baseline.UserMeanBaseline
import org.grouplens.lenskit.baseline.UserMeanItemScorer
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.HLUtilityPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.NDCGPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.knn.item.ItemItemScorer
import org.grouplens.lenskit.knn.item.ModelSize

def dataDir = config['lenskit.movielens.100k']

trainTest {
    dataset crossfold("ML100K") {
        source csvfile("$dataDir/u.data") {
            delimiter "\t"
        }
        partitions 5
        holdout 5
        train 'train.%d.pack'
        test 'test.%d.pack'
        writeTimestamps false
    }

    componentCacheDirectory "cache"
    cacheAllComponents true

    algorithm("Baseline") {
        bind ItemScorer to UserMeanItemScorer
        bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
    }

    algorithm("ItemItem") {
        bind ItemScorer to ItemItemScorer
        bind (BaselineScorer, ItemScorer) to UserMeanItemScorer
        bind (UserMeanBaseline, ItemScorer) to ItemMeanRatingItemScorer
        set ModelSize to 500
    }

    metric CoveragePredictMetric
    metric RMSEPredictMetric
    metric MAEPredictMetric
    metric NDCGPredictMetric
    metric HLUtilityPredictMetric

    output 'results.csv'
    userOutput 'users.csv'
    predictOutput 'predictions.csv'
    recommendOutput 'recommendations.csv.gz'
}
