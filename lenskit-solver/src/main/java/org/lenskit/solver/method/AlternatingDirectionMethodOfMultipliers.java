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
//                                                 + <L, X - Z> + (learningRate / 2) * |X - Z|^2 
// L is the Lagrangian, Z is the introduced constraint variable
public class AlternatingDirectionMethodOfMultipliers extends OptimizationHelper implements OptimizationMethod {

    //l1coef should be greater than zero, since it's almost meaningless to use this method with l1coef 0;
    public void minimize(LearningModel model, ObjectiveFunction objFunc, double tol, int maxIter,
                    double l1coef, double l2coef, double learningRate) throws IOException {
        int numVars = model.getNumOfVariables();
        double[] L = new double[numVars];
        double[] Z = new double[numVars];
        ArrayHelper.randomInitialize(L);
        ArrayHelper.randomInitialize(Z);

        ObjectiveTerminationCriterion outerTermCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        while (outerTermCrit.keepIterate()) {
            ObjectiveTerminationCriterion innerTermCrit = new ObjectiveTerminationCriterion(tol, maxIter);
            double objval = 0;
            while (innerTermCrit.keepIterate()) {
                objval = 0;
                model.startNewIteration();
                LearningOracle orc;
                while ((orc = model.getNextOracle()) != null) {
                    objFunc.wrapOracle(orc);
                    DoubleArrayList varList = orc.getVariables();
                    IntArrayList varIdxList = orc.getVarIndexes();
                    DoubleArrayList gradList = orc.getGradients();
                    for (int i=0; i<varList.size(); i++) {
                        double var = varList.get(i);
                        double grad = gradList.get(i);
                        grad += getL2RegularizerGradient(var, l2coef);
                        int ind = varIdxList.get(i);
                        double newVar = var - learningRate * (grad + L[ind] + 
                                        learningRate * (var - Z[ind]));
                        model.setVariable(i, newVar);
                    }
                    objval += orc.getObjValue();
                }
                //We just use f(X) as the termination criterion to make it simple, because it works.
                innerTermCrit.addIteration(objval);
            }
            for (int i=0; i<Z.length; i++) {
                double var = model.getVariable(i);
                double l = L[i];
                double z = Z[i];
                double signDecider = l + var;
                double newz = var + (l + Math.signum(z) * l1coef) / learningRate;
                if (signDecider * newz < 0) {
                    newz = 0;
                }
                Z[i] = newz;
            }
            for (int i=0; i<L.length; i++) {
                L[i] += learningRate * (model.getVariable(i) - Z[i]);
            }
            //We still use f(X) as the outer termination criterion to make it simple.
            outerTermCrit.addIteration(objval);
        }
    }
}
