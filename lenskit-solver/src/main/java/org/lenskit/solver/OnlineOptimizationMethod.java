package org.lenskit.solver;

public interface OnlineOptimizationMethod extends OptimizationMethod {
    void update(LearningModel learningModel, ObjectiveFunction objectiveFunction);
}
