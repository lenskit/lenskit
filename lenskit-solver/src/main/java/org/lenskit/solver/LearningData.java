package org.lenskit.solver;

public interface LearningData {
    LearningInstance getLearningInstance();
    void startNewIteration();
}
