import org.grouplens.lenskit.eval.data.crossfold.RandomOrder

import org.grouplens.lenskit.knn.NeighborhoodSize
import org.grouplens.lenskit.knn.item.*
import org.grouplens.lenskit.knn.user.*
import org.grouplens.lenskit.transform.normalize.*

import org.apache.commons.lang3.BooleanUtils
import ${package}.ExtendedItemUserMeanScorer

def zipFile = "${config.dataDir}/ml100k.zip"
def dataDir = config.get('mldata.directory', "${config.dataDir}/ml100k")

// This target unpacks the data
target('download') {
    perform {
        // check if the data license is acknowledged
        if (!BooleanUtils.toBoolean(config["grouplens.mldata.acknowledge"])) {
            logger.error(
                    "This analysis makes use of the MovieLens 100K data " +
                            "set from GroupLens Research. Use of this data set is restricted to " +
                            "non-commercial purposes and is only permitted in accordance with the " +
                            "license terms. To use this data in LensKit's automated tests, set the " +
                            "`grouplens.mldata.acknowledge' property to `yes' to indicate you " +
                            "acknowledge the usage license.  More information is available at " +
                            "<http://www.grouplens.org/node/73>.")
            throw new RuntimeException("GroupLens data license not acknoweldged");
        }
    }
    ant.get(src: 'http://www.grouplens.org/system/files/ml-100k.zip',
            dest: zipFile,
            skipExisting: true)
    ant.unzip(src: zipFile, dest: dataDir) {
        patternset {
            include name: 'ml-100k/*'
        }
        mapper type: 'flatten'
    }
}

// this target cross-folds the data. The target object can be used as the data set; it holds
// the value of the last task (in this case, 'crossfold').  The crossfold won't actually be
// avaiable until it is executed, but the evaluator automatically takes care of that.
def ml100k = target('crossfold') {
    requires 'download'

    crossfold {
        source csvfile("${dataDir}/u.data") {
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
}

// Let's define some algorithms
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

// Draw a picture of the custom algorithm
target('draw') {
    dumpGraph {
        output "${config.analysisDir}/extended.dot"
        algorithm extended
    }
}

target('evaluate') {
    // this requires the ml100k target to be run first
    // can either reference a target by object or by name (as above)
    requires ml100k

    trainTest {
        // and just use the target as the data set. The evaluator will do the right thing.
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
}

// After running the evaluation, let's analyze the results
target('analyze') {
    requires 'evaluate'
    // Run R. Note that the script is run in the analysis directory; you might want to
    // copy all R scripts there instead of running them from the source dir.
    ant.exec(executable: config["rscript.executable"], dir: config.analysisDir) {
        arg value: "${config.scriptDir}/chart.R"
    }
}

// By default, run the analyze target
defaultTarget 'analyze'