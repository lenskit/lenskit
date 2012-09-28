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

def ml100k = crossfold {
   source csvfile("ml100k/u.data") {
      delimiter "\t"
      domain {
         minimum 1.0
         maximum 5.0
         precision 1.0
      }
   }
   train "ml100k-crossfold/train.%d.csv"
   test "ml100k-crossfold/test.%d.csv"
   order RandomOrder
   holdout 10
   partitions 5
}

trainTest {
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
