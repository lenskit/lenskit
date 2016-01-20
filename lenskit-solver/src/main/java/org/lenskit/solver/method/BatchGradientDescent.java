package org.grouplens.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;

import org.grouplens.lenskit.mf.svdfeature.ArrayHelper;
import org.grouplens.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l1coef * |X| + l2coef * |X|^2
public class BatchGradientDescent extends OptimizationHelper implements OptimizationMethod {

    public void minimize(LearningModel model, ObjectiveFunction objFunc, double tol, int maxIter,
                    double l1coef, double l2coef, double learningRate) throws IOException {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objval = 0;
        int numVars = model.getNumOfVariables();
        double[] grads = new double[numVars];
        while (termCrit.keepIterate()) {
            objval = 0;
            model.startNewIteration();
            ArrayHelper.initialize(grads, 0);
            LearningOracle orc;
            while ((orc = model.getNextOracle()) != null) {
                objFunc.wrapOracle(orc);
                IntArrayList varIndList = orc.getVarIndexes();
                DoubleArrayList gradList = orc.getGradients();
                for (int i=0; i<varIndList.size(); i++) {
                    double grad = gradList.get(i);
                    int ind = varIndList.get(i);
                    grads[ind] += grad;
                }
                objval += orc.getObjValue();
            }
            for (int i=0; i<numVars; i++) {
                double var = model.getVariable(i);
                double grad = grads[i];
                grad += getRegularizerGradient(var, l1coef, l2coef);
                double newVar = var - learningRate * grad;
                if (var * newVar < 0 && l1coef > 0) {
                    newVar = 0;
                }
                model.setVariable(i, newVar);
            }
            termCrit.addIteration(objval);
        }
    }
}
