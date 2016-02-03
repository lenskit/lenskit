package org.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;

import org.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l2coef * |X|^2
public class StochasticGradientDescent extends OptimizationHelper implements OptimizationMethod {

    public void minimize(LearningModel model, ObjectiveFunction objFunc, int maxIter,
                    double l2coef, double learningRate) throws IOException {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(maxIter);
        double objval = 0;
        while (termCrit.keepIterate()) {
            objval = 0;
            model.startNewIteration();
            StochasticOracle orc;
            while ((orc = model.getStochasticOracle()) != null) {
                objFunc.wrapOracle(orc);
                IntArrayList varIndList = orc.getVarIndexes();
                DoubleArrayList varList = orc.getVariables();
                DoubleArrayList gradList = orc.getGradients();
                for (int i=0; i<varList.size(); i++) {
                    double var = varList.get(i);
                    double grad = gradList.get(i);
                    grad += getRegularizerGradient(var, 0, l2coef);
                    double newVar = var - learningRate * grad;
                    int ind = varIndList.get(i);
                    model.setVariable(ind, newVar);
                }
                objval += orc.getObjValue();
            }
            objval += getRegularizerObjective(model, 0, l2coef);
            termCrit.addIteration(objval);
        }
    }
}
