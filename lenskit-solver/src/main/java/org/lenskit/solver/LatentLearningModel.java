package org.lenskit.solver;

public interface LatentLearningModel extends LearningModel {
    double expectation(LearningInstance ins);
    LearningModel maximization();
}
