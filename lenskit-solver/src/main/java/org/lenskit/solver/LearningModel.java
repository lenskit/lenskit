package org.lenskit.solver;

public interface LearningModel {
    SynchronizedVariableSpace getVariables();
    StochasticOracle getStochasticOracle(LearningInstance ins);
    ObjectiveFunction getObjectiveFunction();
}
