/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.knn.item.ItemSimilarity
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.knn.user.UserSimilarity
import org.grouplens.lenskit.knn.user.UserUserRatingPredictor

import org.grouplens.lenskit.mf.funksvd.FunkSVDRatingPredictor
import org.grouplens.lenskit.mf.funksvd.params.FeatureCount
import org.grouplens.lenskit.mf.funksvd.FunkSVDModelProvider

import org.grouplens.lenskit.slopeone.SlopeOneModel
import org.grouplens.lenskit.slopeone.SlopeOneRatingPredictor
import org.grouplens.lenskit.slopeone.WeightedSlopeOneRatingPredictor

import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.MeanVarianceNormalizer
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer

import org.grouplens.lenskit.util.iterative.StoppingCondition
import org.grouplens.lenskit.util.iterative.IterationCountStoppingCondition
import org.grouplens.lenskit.util.iterative.ThresholdStoppingCondition

def baselines = [GlobalMeanPredictor, UserMeanPredictor, ItemMeanPredictor, ItemUserMeanPredictor]

def ml100k = crossfold {
    source csvfile("${config.dataDir}/ml100k/u.data") {
        delimiter "\t"
        domain {
            minimum 1.0
            maximum 5.0
            precision 1.0
        }
    }
    test "${config.dataDir}/ml100k-crossfold/test.%d.csv"
    train "${config.dataDir}/ml100k-crossfold/train.%d.csv" 
    order RandomOrder
    holdout 10
    partitions 5
}

def UserUser = algorithm("UserUser") {
    bind RatingPredictor to UserUserRatingPredictor
    bind VectorNormalizer to MeanVarianceNormalizer
    bind BaselinePredictor to ItemUserMeanPredictor
    within BaselineSubtractingUserVectorNormalizer bind BaselinePredictor to UserMeanPredictor
    bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
    within UserSimilarity set Damping to 100.0d
    set NeighborhoodSize to 30
}

def ItemItem = algorithm("ItemItem") {
    bind RatingPredictor to ItemItemRatingPredictor
    bind BaselinePredictor to ItemUserMeanPredictor
    bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
    within ItemSimilarity set Damping to 100.0d
    set NeighborhoodSize to 30
}

def FunkSVD = algorithm("FunkSVD") {
    bind RatingPredictor to FunkSVDRatingPredictor
    bind BaselinePredictor to ItemUserMeanPredictor
    set FeatureCount to 30
    within(FunkSVDModelProvider) {
        bind StoppingCondition to IterationCountStoppingCondition
        set IterationCount to 100
    }
    within(FunkSVDRatingPredictor) {
        bind StoppingCondition to ThresholdStoppingCondition
        set ThresholdValue to 0.01
        set MinimumIterations to 10
    }
}

def SlopeOne = algorithm("SlopeOne") {
    within BaselineSubtractingUserVectorNormalizer bind BaselinePredictor to GlobalMeanPredictor
    bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
    bind RatingPredictor to SlopeOneRatingPredictor
    bind BaselinePredictor to ItemUserMeanPredictor
    within SlopeOneModel set Damping to 0
}

def WeightedSlopeOne = algorithm("WeightedSlopeOne") {
    within BaselineSubtractingUserVectorNormalizer bind BaselinePredictor to GlobalMeanPredictor
    bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
    bind RatingPredictor to WeightedSlopeOneRatingPredictor
    bind BaselinePredictor to ItemUserMeanPredictor
    within SlopeOneModel set Damping to 0
}

def algorithms = []

for (bl in baselines) {
    algorithms += algorithm(bl.simpleName.replaceFirst(/Predictor$/, "")) {
        bind RatingPredictor to BaselineRatingPredictor
        bind BaselinePredictor to bl
    }
}

algorithms += UserUser
algorithms += ItemItem
algorithms += FunkSVD
algorithms += SlopeOne
algorithms += WeightedSlopeOne

for (a in algorithms) {
    dumpGraph {
        domain {
            minimum 1.0
            maximum 5.0
            precision 1.0
        }
        output "${config.analysisDir}/${a.name}.csv"
        algorithm a
    }
}

trainTest {
    dataset ml100k
    
    // Three different types of output for analysis.
    output "${config.analysisDir}/eval-results.csv"
    predictOutput "${config.analysisDir}/eval-preds.csv"
    userOutput "${config.analysisDir}/eval-user.csv"
    
    metric CoveragePredictMetric
    metric MAEPredictMetric
    metric RMSEPredictMetric
    metric NDCGPredictMetric
    
    for (a in algorithms) {
        algorithm a
    }
}
