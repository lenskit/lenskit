package org.lenskit.solver;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l2coef * |X|^2
public class BatchGradientDescent implements OptimizationMethod {
    private HashMap<String, RealVector> scalarGrads;
    private HashMap<String, List<RealVector>> vectorGrads;

    private int maxIter;
    private double l2coef;
    private double lr;
    private double tol;

    public BatchGradientDescent() {
        maxIter = 50;
        l2coef = 0.01;
        lr = 10e-6;
        tol = 1.0;
        scalarGrads = new HashMap<>();
        vectorGrads = new HashMap<>();
    }

    public BatchGradientDescent(int inMaxIter, double inL2coef, double inLearningRate, double inTol) {
        maxIter = inMaxIter;
        l2coef = inL2coef;
        lr = inLearningRate;
        tol = inTol;
        scalarGrads = new HashMap<>();
        vectorGrads = new HashMap<>();
    }

    private void assignGrads(HashMap<String, RealVector> scalarVars, HashMap<String, List<RealVector>> vectorVars) {
        for (Map.Entry<String, RealVector> entry : scalarVars.entrySet()) {
            String name = entry.getKey();
            RealVector var = entry.getValue();
            RealVector grad = MatrixUtils.createRealVector(new double[var.getDimension()]);
            grad.set(0.0);
            scalarGrads.put(name, grad);
        }
        for (Map.Entry<String, List<RealVector>> entry : vectorVars.entrySet()) {
            String name = entry.getKey();
            List<RealVector> var = entry.getValue();
            List<RealVector> grad = new ArrayList<>(var.size());
            for (RealVector oneVar : var) {
                grad.add(MatrixUtils.createRealVector(new double[oneVar.getDimension()]));
            }
            vectorGrads.put(name, grad);
        }
    }

    private void updateVars(HashMap<String, RealVector> scalarVars, HashMap<String, List<RealVector>> vectorVars) {
        for (Map.Entry<String, RealVector> entry : scalarVars.entrySet()) {
            String name = entry.getKey();
            RealVector var = entry.getValue();
            var.combineToSelf(1.0, -lr, scalarGrads.get(name));
        }
        for (Map.Entry<String, List<RealVector>> entry : vectorVars.entrySet()) {
            String name = entry.getKey();
            List<RealVector> var = entry.getValue();
            List<RealVector> grad = vectorGrads.get(name);
            for (int i=0; i<var.size(); i++) {
                var.get(i).combineToSelf(1.0, -1.0, grad.get(i).mapMultiplyToSelf(lr));
            }
        }
    }

    public double minimize(LearningModel model, ObjectiveFunction objFunc) {
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        VariableManager variableManager = model.getVariables();
        HashMap<String, RealVector> scalarVars = variableManager.scalarVars;
        HashMap<String, List<RealVector>> vectorVars = variableManager.vectorVars;
        assignGrads(scalarVars, vectorVars);
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
                    scalarGrads.get(name).addToEntry(idx, grad + l2coef * l2term.getGradient(var));
                }
                for (int i=0; i<orc.vectorNames.size(); i++) {
                    String name = orc.vectorNames.get(i);
                    int idx = orc.vectorIndexes.get(i);
                    RealVector var = vectorVars.get(name).get(idx);
                    RealVector grad = orc.vectorGrads.get(i);
                    vectorGrads.get(name).get(idx).combineToSelf(1.0, 1.0, l2term.addGradient(grad, var, l2coef));
                }
                objVal += orc.objVal;
            }
            for (RealVector var : scalarVars.values()) {
                objVal += l2term.getObjective(l2coef, var);
            }
            for (List<RealVector> vars : vectorVars.values()) {
                objVal += l2term.getObjective(l2coef, vars);
            }
            updateVars(scalarVars, vectorVars);
            termCrit.addIteration("BatchGradientDescent", objVal);
        }
        return objVal;
    }
}
