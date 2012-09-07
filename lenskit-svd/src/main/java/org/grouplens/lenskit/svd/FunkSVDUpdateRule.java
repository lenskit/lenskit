package org.grouplens.lenskit.svd;

import javax.inject.Inject;

import org.grouplens.lenskit.svd.params.IterationCount;
import org.grouplens.lenskit.svd.params.LearningRate;
import org.grouplens.lenskit.svd.params.RegularizationTerm;
import org.grouplens.lenskit.svd.params.TrainingThreshold;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;

public class FunkSVDUpdateRule {

	private final int iterationCount;
    private final double learningRate;
    private final double trainingThreshold;
    private final double trainingRegularization;
    private final ClampingFunction clampingFunction;
    
    @Inject
    public FunkSVDUpdateRule(@LearningRate double rate, @TrainingThreshold double threshold,
            @RegularizationTerm double gradientDescent, ClampingFunction clamp,
            @IterationCount int iterCount) {
    	learningRate = rate;
        trainingThreshold = threshold;
        trainingRegularization = gradientDescent;
        clampingFunction = clamp;
        iterationCount = iterCount;
    }
    
    public FunkSVDFeatureTrainer getTrainer() {
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
