package org.lenskit.solver.method;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.solver.objective.*;

import java.util.HashMap;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l2coef * |X|^2
public class StochasticGradientDescent implements OptimizationMethod {
    private int maxIter;
    private double l2coef;
    private double lr;
    private double tol;

    public StochasticGradientDescent() {
        maxIter = 50;
        l2coef = 0.01;
        lr = 0.005;
        tol = 1.0;
    }

    public StochasticGradientDescent(int inMaxIter, double inL2coef, double inLearningRate, double inTol) {
        maxIter = inMaxIter;
        l2coef = inL2coef;
        lr = inLearningRate;
        tol = inTol;
    }

    public void minimize(LearningModel model, ObjectiveFunction objFunc) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        HashMap<String, RealVector> scalarVars = model.getScalarVars();
        HashMap<String, RealMatrix> vectorVars = model.getVectorVars();
        L2Regularizer l2term = new L2Regularizer();
        double objVal = 0;
        while (termCrit.keepIterate()) {
            objVal = 0;
            model.startNewIteration();
            LearningInstance ins;
            StochasticOracle orc;
            while ((ins = model.getLearningInstance()) != null) {
                orc = model.getStochasticOracle(ins);
                objFunc.wrapOracle(orc);
                for (int i=0; i<orc.scalarNames.size(); i++) {
                    String name = orc.scalarNames.get(i);
                    int idx = orc.scalarIndexes.get(i);
                    double grad = orc.scalarGrads.get(i);
                    double var = scalarVars.get(name).getEntry(idx);
                    scalarVars.get(name).setEntry(idx, var - lr * (grad + l2coef * l2term.getGradient(var)));
                }
                for (int i=0; i<orc.vectorNames.size(); i++) {
                    String name = orc.vectorNames.get(i);
                    int idx = orc.vectorIndexes.get(i);
                    RealVector grad = orc.vectorGrads.get(i);
                    RealVector var = vectorVars.get(name).getRowVector(idx);
                    var.combineToSelf(1.0, -lr, l2term.addGradient(grad, var, l2coef));
                }
                objVal += orc.objVal;
            }
            for (RealVector var : scalarVars.values()) {
                double l2norm = var.getNorm();
                objVal += (l2coef * l2norm * l2norm);
            }
            for (RealMatrix var : vectorVars.values()) {
                double fnorm = var.getFrobeniusNorm();
                objVal += (l2coef * fnorm * fnorm);
            }
            termCrit.addIteration(objVal);
        }
        return objVal;
    }
}
