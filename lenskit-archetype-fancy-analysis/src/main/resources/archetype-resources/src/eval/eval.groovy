import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor
import org.grouplens.lenskit.knn.item.ItemSimilarity
import org.grouplens.lenskit.knn.params.NeighborhoodSize
import org.grouplens.lenskit.knn.user.UserSimilarity
import org.grouplens.lenskit.knn.user.UserUserRatingPredictor

import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.MeanVarianceNormalizer
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer
import org.grouplens.lenskit.transform.normalize.VectorNormalizer

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
	bind RatingPredictor to ItemItemRatingPredictor
	bind BaselinePredictor to ItemUserMeanPredictor
	bind VectorNormalizer to MeanVarianceNormalizer
	bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
	within ItemSimilarity set Damping to 100.0d
	set NeighborhoodSize to 30
}

def useruser = algorithm("UserUser") {
	bind RatingPredictor to UserUserRatingPredictor
	bind BaselinePredictor to ItemUserMeanPredictor
	bind VectorNormalizer to MeanVarianceNormalizer
	bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
	set NeighborhoodSize to 30
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
