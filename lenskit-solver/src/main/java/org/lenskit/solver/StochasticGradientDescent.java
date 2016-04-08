package org.lenskit.solver;

import org.apache.commons.math3.linear.RealVector;

import java.util.List;

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

    public double minimize(LearningModel model, LearningData learningData) {
        ObjectiveFunction objFunc = model.getObjectiveFunction();
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        L2Regularizer l2term = new L2Regularizer();
        double objVal = 0;
        while (termCrit.keepIterate()) {
            objVal = 0;
            List<String> allScalarVarNames = model.getAllScalarVarNames();
            for (String name : allScalarVarNames) {
                RealVector var = model.getScalarVarByName(name);
                objVal += l2term.getObjective(l2coef, var);
            }
            List<String> allVectorVarNames = model.getAllVectorVarNames();
            for (String name : allVectorVarNames) {
                List<RealVector> vars = model.getVectorVarByName(name);
                objVal += l2term.getObjective(l2coef, vars);
            }
            learningData.startNewIteration();
            LearningInstance ins;
            StochasticOracle orc;
            while ((ins = learningData.getLearningInstance()) != null) {
                orc = model.getStochasticOracle(ins);
                objFunc.wrapOracle(orc);
                for (int i=0; i<orc.scalarNames.size(); i++) {
                    String name = orc.scalarNames.get(i);
                    int idx = orc.scalarIndexes.get(i);
                    double grad = orc.scalarGrads.get(i);
                    double var = model.getScalarVarByNameIndex(name, idx);
                    model.setScalarVarByNameIndex(name, idx, var - lr * (grad + l2coef * l2term.getGradient(var)));
                }
                for (int i=0; i<orc.vectorNames.size(); i++) {
                    String name = orc.vectorNames.get(i);
                    int idx = orc.vectorIndexes.get(i);
                    RealVector var = model.getVectorVarByNameIndex(name, idx);
                    RealVector grad = orc.vectorGrads.get(i);
                    model.setVectorVarByNameIndex(name, idx, var.combineToSelf(1.0, -lr,
                                                                               l2term.addGradient(grad, var, l2coef)));
                }
                objVal += orc.objVal;
            }
            termCrit.addIteration("StochasticGradientDescent", objVal);
        }
        return objVal;
    }
}
