package org.grouplens.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;

import org.grouplens.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l1coef * |X| + l2coef * |X|^2
public class AlternatingStochasticGradientDescent extends OptimizationHelper implements OptimizationMethod {

    public void minimize(LearningModel model, ObjectiveFunction objFunc, double tol, int maxIter,
                    double l1coef, double l2coef, double learningRate) throws IOException {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objval = 0;
        int numAlter = model.getNumOfAlternation();
        while (termCrit.keepIterate()) {
            for (int k=0; k<numAlter; k++) {
                ObjectiveTerminationCriterion innerTermCrit = new ObjectiveTerminationCriterion(tol, maxIter);
                while (innerTermCrit.keepIterate()) {
                    objval = 0;
                    model.startNewIteration();
                    LearningOracle orc;
                    while ((orc = model.getNextAlternatingOracle(k)) != null) {
                        objFunc.wrapOracle(orc);
                        IntArrayList varIndList = orc.getVarIndexes();
                        DoubleArrayList gradList = orc.getGradients();
                        DoubleArrayList varList = orc.getVariables();
                        for (int i=0; i<varIndList.size(); i++) {
                            int ind = varIndList.get(i);
                            double grad = gradList.get(i);
                            double var = varList.get(i);
                            grad += getRegularizerGradient(var, l1coef, l2coef);
                            double newVar = var - learningRate * grad;
                            if (var * newVar < 0 && l1coef > 0) {
                                newVar = 0;
                            }
                            model.setVariable(ind, newVar);
                        }
                        objval += orc.getObjValue();
                    }
                    innerTermCrit.addIteration(objval);
                }
            }
            termCrit.addIteration(objval);
        }
    }
}
