package org.lenskit.solver;

public interface OnlineOptimizationMethod extends OptimizationMethod {
    double update(LearningModel learningModel, LearningData learningData);
}
