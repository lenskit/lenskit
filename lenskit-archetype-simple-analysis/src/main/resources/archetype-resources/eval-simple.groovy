import org.grouplens.lenskit.RatingPredictor
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

import org.grouplens.lenskit.params.Damping
import org.grouplens.lenskit.baseline.*

def ml100k = crossfold("ml-100k") {
   source csvfile("ml-100k/u.data") {
      delimiter "\t"
      domain {
         minimum 1.0
         maximum 5.0
         precision 1.0
      }
   }
   train "split/ml-100k.%d.train.dat"
   test "split/ml-100k.%d.test.dat"
   order RandomOrder
   holdout 10
   partitions 5
}

trainTest("item-item vs. user-user algorithm") {
   dataset ml100k
   
   // Three different types of output for analysis.
   output "eval-results.csv"
   predictOutput "eval-preds.csv"
   userOutput "eval-user.csv"
   
   metric CoveragePredictMetric
   metric RMSEPredictMetric
   metric NDCGPredictMetric
   
   algorithm("ItemItem") {
      bind RatingPredictor to ItemItemRatingPredictor
      bind BaselinePredictor to ItemUserMeanPredictor
      bind VectorNormalizer to MeanVarianceNormalizer
      bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
      within ItemSimilarity set Damping to 100.0d
      set NeighborhoodSize to 30
   }

   algorithm("UserUser") {
      bind RatingPredictor to UserUserRatingPredictor
      bind BaselinePredictor to ItemUserMeanPredictor
      bind VectorNormalizer to MeanVarianceNormalizer
      bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
      set NeighborhoodSize to 30
   }
}
