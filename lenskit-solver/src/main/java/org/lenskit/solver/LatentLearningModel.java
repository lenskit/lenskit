package org.lenskit.solver;

public interface LatentLearningModel {
    double expectation(LearningInstance ins);
    LearningModel maximization();
}
