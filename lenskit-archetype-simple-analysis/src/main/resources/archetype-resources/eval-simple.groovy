import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.knn.NeighborhoodSize
import org.grouplens.lenskit.knn.item.*
import org.grouplens.lenskit.knn.user.*

import org.grouplens.lenskit.transform.normalize.*

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
        // use the item-item rating predictor with a baseline and normalizer
        bind ItemScorer to ItemItemScorer
        bind BaselinePredictor to ItemUserMeanPredictor
        bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer

        // retain 500 neighbors in the model, use 30 for prediction
        set ModelSize to 500
        set NeighborhoodSize to 30

        // apply some Bayesian smoothing to the mean values
        within(ItemUserMeanPredictor) {
            set MeanDamping to 25.0d
        }
    }

    algorithm("UserUser") {
        // use the user-user rating predictor
        bind ItemScorer to UserUserItemScorer
        bind BaselinePredictor to ItemUserMeanPredictor
        bind VectorNormalizer to MeanVarianceNormalizer

        // use 30 neighbors for predictions
        set NeighborhoodSize to 30

        // override normalizer within the neighborhood finder
        // this makes it use a different normalizer (subtract user mean) for computing
        // user similarities
        within(NeighborhoodFinder) {
            bind UserVectorNormalizer to BaselineSubtractingUserVectorNormalizer
            // override baseline to use user mean
            bind BaselinePredictor to UserMeanPredictor
        }

        // and apply some Bayesian damping to the baseline
        within(ItemUserMeanPredictor) {
            set MeanDamping to 25.0d
        }
    }
}
