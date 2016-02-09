package org.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExpectationMaximization {
    private int maxIter;
    private double tol;
    private OptimizationMethod method;
    
    public ExpectationMaximization() {
        maxIter = 50;
        tol = 1.0;
        method = new BatchGradientDescent();
    }

    public void minimize(LatentVariableModel model, ObjectiveFunction objFunc) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter * 2);
        while (termCrit.keepIterate()) {
            double objVal = 0;
            model.startNewIteration();
            LearningInstance ins;
            while ((ins = model.getLearningInstance()) != null) {
                objVal += model.expection(ins);
            }
            LearningModel subModel = model.maximization();
            if (subModel != null) {
                method.minimize(subModel, objFunc);
            }
        }
    }
}
