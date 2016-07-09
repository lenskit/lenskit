package org.lenskit.solver;

abstract public class AbstractOptimizationMethod implements OptimizationMethod {
    public double evaluate(LearningModel model, LearningData validData) {
        double objVal = 0.0;
        ObjectiveFunction objFunc = model.getObjectiveFunction();
        LearningInstance ins;
        validData.startNewIteration();
        while ((ins = validData.getLearningInstance()) != null) {
            StochasticOracle orc = model.getStochasticOracle(ins);
            objFunc.wrapOracle(orc);
            objVal += orc.objVal;
        }
        return objVal;
    }
}
