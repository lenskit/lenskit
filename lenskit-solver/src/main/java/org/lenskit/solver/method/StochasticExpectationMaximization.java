package org.lenskit.solver.method;

import org.lenskit.solver.objective.LearningInstance;
import org.lenskit.solver.objective.LearningModel;
import org.lenskit.solver.objective.ObjectiveFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StochasticExpectationMaximization implements OptimizationMethod {
    private static Logger logger = LoggerFactory.getLogger(StochasticExpectationMaximization.class);
    private int maxIter;
    private double tol;
    private OptimizationMethod method;
    
    public StochasticExpectationMaximization() {
        maxIter = 50;
        tol = 1.0;
        method = new BatchGradientDescent(3, 0.0, 10e-3, 1.0);
    }

    public double minimize(LearningModel model, ObjectiveFunction objFunc) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objVal = 0.0;
        while (termCrit.keepIterate()) {
            objVal = 0.0;
            model.startNewIteration();
            LearningInstance ins;
            while ((ins = model.getLearningInstance()) != null) {
                objVal += model.stochastic_expectation(ins);
                LearningModel subModel = model.stochastic_maximization();
                if (subModel != null) {
                    objVal += method.minimize(subModel, objFunc);
                }
            }
            model.maximization();
            termCrit.addIteration("StochasticExpectationMaximization", objVal);
        }
        return objVal;
    }
}
