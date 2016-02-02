package org.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;

import org.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l1coef * |X| + l2coef * |X|^2
public class StochasticGradientDescent extends OptimizationHelper implements OptimizationMethod {

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
                IntArrayList varIndList = orc.getVarIndexes();
                DoubleArrayList varList = orc.getVariables();
                DoubleArrayList gradList = orc.getGradients();
                for (int i=0; i<varList.size(); i++) {
                    double var = varList.get(i);
                    double grad = gradList.get(i);
                    grad += getRegularizerGradient(var, l1coef, l2coef);
                    double newVar = var - learningRate * grad;
                    if (var * newVar < 0 && l1coef > 0) {
                        newVar = 0;
                    }
                    int ind = varIndList.get(i);
                    model.setVariable(ind, newVar);
                }
                objval += orc.getObjValue();
            }
            //Probably we don't need calculate this, because it is used for termination decision.
            //objval += getRegularizerObjective(model, l1coef, l2coef);
            //We only use f(X) to decide the termination of iteration.
            termCrit.addIteration(objval);
        }
    }
}
