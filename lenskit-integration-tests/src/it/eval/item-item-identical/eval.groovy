/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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


import org.grouplens.lenskit.eval.metrics.predict.*
import org.grouplens.lenskit.transform.normalize.MeanCenteringVectorNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer
import org.grouplens.lenskit.transform.truncate.VectorTruncator
import org.lenskit.api.ItemScorer
import org.lenskit.api.RatingPredictor
import org.lenskit.baseline.BaselineScorer
import org.lenskit.baseline.ItemMeanRatingItemScorer
import org.lenskit.knn.NeighborhoodSize
import org.lenskit.knn.item.ItemItemScorer
import org.lenskit.knn.item.ModelSize
import org.lenskit.knn.item.model.*

def dataDir = config['lenskit.movielens.100k']

trainTest {
    dataset crossfold("ML100K") {
        source csvfile("$dataDir/u.data") {
            delimiter "\t"
        }
        partitions 5
        holdout 5
        train 'train.%d.csv'
        test 'test.%d.csv'
    }

    def common = {
        bind ItemScorer to ItemItemScorer
        set NeighborhoodSize to 20
        set ModelSize to 500
        bind ItemItemBuildContext toProvider ItemwiseBuildContextProvider
        within (ItemItemBuildContext) {
            bind VectorNormalizer to MeanCenteringVectorNormalizer
        }
        bind (BaselineScorer, ItemScorer) to ItemMeanRatingItemScorer
        at (RatingPredictor) {
            // turn off baselines - make sure everything produces the same recs
            bind (BaselineScorer, ItemScorer) to null
        }
    }

    algorithm("Standard") {
        include common
    }
    algorithm("Normalizing") {
        include common
        bind ItemItemModel toProvider NormalizingItemItemModelBuilder
        at (ItemItemModel) {
            bind VectorTruncator toProvider StandardVectorTruncatorProvider
        }
    }

    metric CoveragePredictMetric
    metric RMSEPredictMetric
    metric MAEPredictMetric
    metric NDCGPredictMetric
    metric HLUtilityPredictMetric

    output 'results.csv'
    predictOutput 'predictions.csv'
    writePredictionChannel ItemItemScorer.NEIGHBORHOOD_SIZE_SYMBOL, "NbrCount"
}
