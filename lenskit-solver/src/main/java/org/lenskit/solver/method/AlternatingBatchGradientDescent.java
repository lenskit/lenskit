package org.lenskit.solver.method;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;

import org.lenskit.mf.svdfeature.ArrayHelper;
import org.lenskit.solver.objective.ObjectiveFunction;

// Objective function is changed from f(X) to f(X) + l1coef * |X| + l2coef * |X|^2
public class AlternatingBatchGradientDescent extends OptimizationHelper implements OptimizationMethod {

    public void minimize(LearningModel model, ObjectiveFunction objFunc, double tol, int maxIter,
                    double l1coef, double l2coef, double learningRate) throws IOException {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objval = 0;
        int numVars = model.getNumOfVariables();
        int numAlter = model.getNumOfAlternation();
        double[] grads = new double[numVars];
        while (termCrit.keepIterate()) {
            for (int k=0; k<numAlter; k++) {
                ObjectiveTerminationCriterion innerTermCrit = new ObjectiveTerminationCriterion(tol, maxIter);
                while (innerTermCrit.keepIterate()) {
                    objval = 0;
                    model.startNewIteration();
                    ArrayHelper.initialize(grads, Double.MAX_VALUE);
                    LearningOracle orc;
                    while ((orc = model.getNextAlternatingOracle(k)) != null) {
                        objFunc.wrapOracle(orc);
                        IntArrayList varIndList = orc.getVarIndexes();
                        DoubleArrayList gradList = orc.getGradients();
                        for (int i=0; i<varIndList.size(); i++) {
                            int ind = varIndList.get(i);
                            double grad = gradList.get(i);
                            if (grads[ind] == Double.MAX_VALUE) {
                                grads[ind] = 0;
                            }
                            grads[ind] += grad;
                        }
                        objval += orc.getObjValue();
                    }
                    for (int i=0; i<numVars; i++) {
                        double var = model.getVariable(i);
                        double grad = grads[i];
                        if (grad != Double.MAX_VALUE) {
                            grad += getRegularizerGradient(var, l1coef, l2coef);
                            double newVar = var - learningRate * grad;
                            if (var * newVar < 0 && l1coef > 0) {
                                newVar = 0;
                            }
                            model.setVariable(i, newVar);
                        }
                    }
                    innerTermCrit.addIteration(objval);
                }
            }
            termCrit.addIteration(objval);
        }
    }
}
