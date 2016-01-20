package org.grouplens.lenskit.solver.method;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class OptimizationHelper {
    private L1Regularizer l1term;
    private L2Regularizer l2term;

    public OptimizationHelper() {
        l1term = new L1Regularizer();
        l2term = new L2Regularizer();
    }

    protected double getL2RegularizerGradient(double var, double l2coef) {
        return l2coef * l2term.getGradient(var);
    }

    protected double getRegularizerGradient(double var, double l1coef, double l2coef) {
        double grad = 0;
        if (l1coef > 0) {
            grad += l1coef * l1term.getSubGradient(var);
        }
        if (l2coef > 0) {
            grad += l2coef * l2term.getGradient(var);
        }
        return grad;
    }

    protected double getRegularizerObjective(LearningModel model, double l1coef, double l2coef) {
        double objval = 0;
        int numVars = model.getNumOfVariables();
        if (l1coef > 0 || l2coef > 0) {
            for (int i=0; i<numVars; i++) {
                double var = model.getVariable(i);
                if (l1coef > 0) {
                    objval += l1coef * l1term.getValue(var);
                }
                if (l2coef > 0) {
                    objval += l2coef * l2term.getValue(var);
                }
            }
        }
        return objval;
    }
}
