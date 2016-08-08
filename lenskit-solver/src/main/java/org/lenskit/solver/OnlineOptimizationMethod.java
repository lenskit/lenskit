package org.lenskit.solver;

import org.grouplens.grapht.annotation.DefaultImplementation;

@DefaultImplementation(StochasticGradientDescent.class)
public interface OnlineOptimizationMethod extends OptimizationMethod {
    double update(LearningModel learningModel, LearningData learningData);
}
