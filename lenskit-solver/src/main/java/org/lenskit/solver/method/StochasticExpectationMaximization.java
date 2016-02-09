package org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StochasticExpectationMaximization {
    private int maxIter;
    private double tol;
    private OptimizationMethod method;
    
    public StochasticExpectationMaximization() {
        maxIter = 50;
        tol = 1.0;
        method = new BatchGradientDescent();
    }

    public void minimize(LatentVariableModel model, ObjectiveFunction objFunc) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        while (termCrit.keepIterate()) {
            double objVal = 0.0;
            model.startNewIteration();
            LearningInstance ins;
            while ((ins = model.getLearningInstance()) != null) {
                objVal += model.expectation(ins);
                LearningModel subModel = model.maximization();
                if (subModel != null) {
                    method.minimize(subModel, objFunc);
                }
            }
            termCrit.addIteration(objVal);
        }
    }
}
