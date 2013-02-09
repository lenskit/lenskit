import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.knn.params.*
import org.grouplens.lenskit.knn.item.*
import org.grouplens.lenskit.knn.user.*

import org.grouplens.lenskit.transform.normalize.*

import ${package}.ExtendedItemUserMeanPredictor

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

def itemitem = algorithm("ItemItem") {
    // use the item-item rating predictor
    bind RatingPredictor to ItemItemRatingPredictor
    // use item-user personalized means as the baseline
    bind BaselinePredictor to ItemUserMeanPredictor
    // normalize ratings by subtracting the baseline prior to model building
    bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
    // use 30 neighbors for prediction
    set NeighborhoodSize to 30
    // retain 500 neighbors in the model
    set ModelSize to 500
    // apply some Bayesian smoothing to the mean values
    within(ItemUserMeanPredictor) {
        set Damping to 25.0d
    }
}

def useruser = algorithm("UserUser") {
    // use the user-user rating predictor
    bind RatingPredictor to UserUserRatingPredictor
    // use item-user personalized means as the baseline
    bind BaselinePredictor to ItemUserMeanPredictor
    // normalize ratings by mean-variance (z-score) in predictions
    bind VectorNormalizer to MeanVarianceNormalizer
    // override normalizer within the neighborhood finder
    // this makes it use a different normalizer (subtract user mean) for computing
    // user similarities
    within(NeighborhoodFinder) {
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
        // override baseline to use user mean
        bind BaselinePredictor to UserMeanPredictor
    }
    // use 30 neighbors for predictions
    set NeighborhoodSize to 30
    // and apply some Bayesian damping to the baseline
    within(ItemUserMeanPredictor) {
        set Damping to 25.0d
    }
}

def extended = algorithm("ExtendedItemUserMean") {
	bind RatingPredictor to ExtendedItemUserMeanPredictor
}

dumpGraph {
	output "${config.analysisDir}/extended.gv"
	algorithm extended
}

trainTest {
	dataset ml100k

	// Three different types of output for analysis.
	output "${config.analysisDir}/eval-results.csv"
	predictOutput "${config.analysisDir}/eval-preds.csv"
	userOutput "${config.analysisDir}/eval-user.csv"

	metric CoveragePredictMetric
	metric RMSEPredictMetric
	metric NDCGPredictMetric

	algorithm itemitem
	algorithm useruser
	algorithm extended
}
