package org.grouplens.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.io.IOException;

import org.grouplens.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to <f'(X), X> + l1coef * |X| + l2coef * |X|^2
//                                                       + 1/(2*learningRate) * |X - Xt|^2 
public class CompositeObjectiveGradientDescent extends OptimizationHelper implements OptimizationMethod {

    //l1coef should be greater than zero, since it's meaningless to use this method with l1coef 0;
    //       with l1coef 0, it's exactly Stochastic Gradient Descent
    public void minimize(LearningModel model, ObjectiveFunction objFunc, double tol, int maxIter,
                    double l1coef, double l2coef, double learningRate) throws IOException {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objval = 0;
        while (termCrit.keepIterate()) {
            objval = 0;
            model.startNewIteration();
            LearningOracle orc;
            while ((orc = model.getNextOracle()) != null) {
                objFunc.wrapOracle(orc);
                DoubleArrayList varList = orc.getVariables();
                DoubleArrayList gradList = orc.getGradients();
                for (int i=0; i<varList.size(); i++) {
                    double var = varList.get(i);
                    double grad = gradList.get(i);
                    grad += getL2RegularizerGradient(var, l2coef);
                    double signDecider = var / learningRate - grad;
                    double newVar = var - learningRate * (grad + Math.signum(var) * l1coef);
                    if (signDecider * newVar < 0) {
                        newVar = 0;
                    }
                    model.setVariable(i, newVar);
                }
                objval += orc.getObjValue();
            }
            termCrit.addIteration(objval);
        }
    }
}
