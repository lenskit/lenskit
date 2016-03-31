package org.lenskit.solver;

public interface LearningModel {
    VariableManager getVariables();
    LearningInstance getLearningInstance();
    void startNewIteration();
    StochasticOracle getStochasticOracle(LearningInstance ins);
}
