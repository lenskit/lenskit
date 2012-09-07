package org.grouplens.lenskit.svd;

import javax.inject.Inject;

import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.svd.params.RegularizationTerm;
import org.grouplens.lenskit.svd.params.TrainingThreshold;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;

/**
 * Configuration for computing FunkSVD updates. The config produces
 * {@link FunkSVDFeatureTrainer}s to train individual features.
 *
 * @since 1.0
 */
public class FunkSVDTrainingConfig {

    private final int iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;

    /**
     * Construct a new FunkSVD configuration.
     *
     * @param lrate The learning rate.
     * @param threshold The training threshold (stop after delta RMSE drops below this).
     * @param reg The regularization term.
     * @param clamp The clamping function.
     * @param iterCount The max iteration count.
     */
    @Inject
    public FunkSVDTrainingConfig(@LearningRate double lrate,
                                 @TrainingThreshold double threshold,
                                 @RegularizationTerm double reg,
                                 ClampingFunction clamp,
                                 @IterationCount int iterCount) {
        learningRate = lrate;
        trainingThreshold = threshold;
        trainingRegularization = reg;
        clampingFunction = clamp;
        iterationCount = iterCount;
    }

    /**
     * Create a new trainer using this configuration.
     *
     * @return A new feature trainer for iteratively training a single
     *         FunkSVD feature.
     */
    public FunkSVDFeatureTrainer newTrainer() {
        return new FunkSVDFeatureTrainer(learningRate, trainingThreshold,
                                         trainingRegularization, clampingFunction, iterationCount);
    }

    public double getIterationCount() {
        return iterationCount;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getTrainingThreshold() {
        return trainingThreshold;
    }

    public double getTrainingRegularization() {
        return trainingRegularization;
    }

    public ClampingFunction getClampingFunction() {
        return clampingFunction;
    }
}
