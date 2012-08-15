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

import org.grouplens.lenskit.RatingPredictor
import org.grouplens.lenskit.eval.data.crossfold.RandomOrder
import org.grouplens.lenskit.eval.metrics.predict.CoveragePredictMetric
import org.grouplens.lenskit.eval.metrics.predict.MAEPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.NDCGPredictMetric
import org.grouplens.lenskit.eval.metrics.predict.RMSEPredictMetric
import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.knn.item.ItemSimilarity
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.knn.user.UserSimilarity
import org.grouplens.lenskit.knn.user.UserUserRatingPredictor
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.MeanVarianceNormalizer
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer
import org.grouplens.lenskit.params.Damping
import org.grouplens.lenskit.slopeone.SlopeOneModel
import org.grouplens.lenskit.slopeone.SlopeOneRatingPredictor
import org.grouplens.lenskit.slopeone.WeightedSlopeOneRatingPredictor
import org.grouplens.lenskit.svd.FunkSVDRatingPredictor
import org.grouplens.lenskit.svd.params.FeatureCount
import org.grouplens.lenskit.svd.params.IterationCount
import org.grouplens.lenskit.baseline.*
import org.grouplens.lenskit.svd.FunkSVDModelProvider

def baselines = [GlobalMeanPredictor, UserMeanPredictor, ItemMeanPredictor, ItemUserMeanPredictor]

def buildDir = System.getProperty("project.build.directory", ".")

def ml100k = crossfold("ml-100k") {
    source csvfile("${buildDir}/ml-100k/u.data") {
        delimiter "\t"
        domain {
            minimum 1.0
            maximum 5.0
            precision 1.0
        }
    }
    order RandomOrder
    holdout 10
    partitions 5
    train "${buildDir}/ml-100k.train.%d.csv"
    test "${buildDir}/ml-100k.test.%d.csv"
}

trainTest("mutli-algorithm") {
    dataset ml100k

    output "${buildDir}/eval-results.csv"
    predictOutput "${buildDir}/eval-preds.csv"
    userOutput "${buildDir}/eval-user.csv"

    metric CoveragePredictMetric
    metric MAEPredictMetric
    metric RMSEPredictMetric
    metric NDCGPredictMetric

    for (bl in baselines) {
        algorithm(bl.simpleName.replaceFirst(/Predictor$/, "")) {
            bind RatingPredictor to BaselineRatingPredictor
            bind BaselinePredictor to bl
        }
    }

    algorithm("UserUser") {
        bind RatingPredictor to UserUserRatingPredictor
        bind VectorNormalizer to MeanVarianceNormalizer
        bind BaselinePredictor to ItemUserMeanPredictor
        within BaselineSubtractingUserVectorNormalizer bind BaselinePredictor to UserMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        within UserSimilarity bind Double withQualifier Damping to 100.0d
        bind Integer withQualifier NeighborhoodSize to 30
//        setComponent(RatingPredictor, UserUserRatingPredictor)
//        setComnponent(PredictNormalizer, VectorNormalizer, MeanVarianceNormalizer)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        setComponent(NormalizerBaseline, BaselinePredictor, UserMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingUserVectorNormalizer)
//        setComponent(UserSimilarity, Similarity, CosineSimilarity)
//        set(SimilarityDamping, 100)
//        set(NeighborhoodSize, 30)
    }

    algorithm("ItemItem") {
        bind RatingPredictor to ItemItemRatingPredictor
        bind BaselinePredictor to ItemUserMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        within ItemSimilarity bind Double withQualifier Damping to 100.0d
        bind Integer withQualifier NeighborhoodSize to 30
//        setComponent(RatingPredictor, ItemItemRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingUserVectorNormalizer)
//        set(SimilarityDamping, 100)
//        set(NeighborhoodSize, 30);
    }
    
    algorithm("WeightedSlopeOne") {
        within BaselineSubtractingUserVectorNormalizer bind BaselinePredictor to GlobalMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        bind RatingPredictor to WeightedSlopeOneRatingPredictor
        bind BaselinePredictor to ItemUserMeanPredictor
        within SlopeOneModel bind Integer withQualifier Damping to 0
//        setComponent(NormalizerBaseline, BaselinePredictor, GlobalMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingUserVectorNormalizer)
//        setComponent(RatingPredictor, WeightedSlopeOneRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        set(DeviationDamping, 0)
    }

    algorithm("SlopeOne") {
        within BaselineSubtractingUserVectorNormalizer bind BaselinePredictor to GlobalMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        bind RatingPredictor to SlopeOneRatingPredictor
        bind BaselinePredictor to ItemUserMeanPredictor
        within SlopeOneModel bind Integer withQualifier Damping to 0
//        setComponent(NormalizerBaseline, BaselinePredictor, GlobalMeanPredictor)
//        setComponent(UserVectorNormalizer, VectorNormalizer, BaselineSubtractingUserVectorNormalizer)
//        setComponent(RatingPredictor, SlopeOneRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        set(DeviationDamping, 0)
    }

    algorithm("FunkSVD") {
        bind RatingPredictor to FunkSVDRatingPredictor
        bind BaselinePredictor to ItemUserMeanPredictor
        bind Integer withQualifier FeatureCount to 30
        within FunkSVDModelProvider bind Integer withQualifier IterationCount to 100
        within FunkSVDRatingPredictor bind Integer withQualifier IterationCount to 30
//        setComponent(RatingPredictor, FunkSVDRatingPredictor)
//        setComponent(BaselinePredictor, ItemUserMeanPredictor)
//        set(FeatureCount, 30)
//        set(IterationCount, 100)
    }
}
