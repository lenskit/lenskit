import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.knn.params.*
import org.grouplens.lenskit.knn.item.*
import org.grouplens.lenskit.knn.user.*

import org.grouplens.lenskit.transform.normalize.*

import ${package}.ExtendedItemUserMeanScorer

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

def useruser = algorithm("UserUser") {
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

def extended = algorithm("ExtendedItemUserMean") {
    bind ItemScorer to ExtendedItemUserMeanScorer
}

dumpGraph {
    output "${config.analysisDir}/extended.dot"
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
