/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.lenskit.solver;

import org.apache.commons.math3.linear.MatrixUtils;

import org.apache.commons.math3.linear.RealVector;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Batch gradient descent algorithm.
 * Objective function is changed from f(X) to f(X) + l2coef * |X|^2
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public class BatchGradientDescent extends AbstractOptimizationMethod {
    private final HashMap<String, RealVector> scalarGrads = new HashMap<>();
    private final HashMap<String, List<RealVector>> vectorGrads = new HashMap<>();

    private int maxIter;
    private double l2coef;
    private double lr;
    private double tol;

    @Inject
    public BatchGradientDescent() {
        maxIter = 50;
        l2coef = 0.01;
        lr = 10e-6;
        tol = 1.0;
    }

    public BatchGradientDescent(int maxIter, double l2coef, double learningRate, double tol) {
        this.maxIter = maxIter;
        this.l2coef = l2coef;
        this.lr = learningRate;
        this.tol = tol;
    }

    private void assignGrads(LearningModel learningModel) {
        List<String> scalarVarNames = learningModel.getAllScalarVarNames();
        for (String name : scalarVarNames) {
            int varSize = learningModel.getScalarVarSizeByName(name);
            RealVector grad = MatrixUtils.createRealVector(new double[varSize]);
            grad.set(0.0);
            scalarGrads.put(name, grad);
        }
        List<String> vectorVarNames = learningModel.getAllVectorVarNames();
        for (String name : vectorVarNames) {
            int varSize = learningModel.getVectorVarSizeByName(name);
            int dimension = learningModel.getVectorVarDimensionByName(name);
            List<RealVector> grad = new ArrayList<>(varSize);
            for (int i=0; i<varSize; i++) {
                grad.add(MatrixUtils.createRealVector(new double[dimension]));
            }
            vectorGrads.put(name, grad);
    }
    }

    private void updateVars(LearningModel learningModel) {
        List<String> scalarVarNames = learningModel.getAllScalarVarNames();
        for (String name : scalarVarNames) {
            RealVector var = learningModel.getScalarVarByName(name);
            learningModel.setScalarVarByName(name, var.combineToSelf(1.0, -lr, scalarGrads.get(name)));
        }
        List<String> vectorVarNames = learningModel.getAllVectorVarNames();
        for (String name : vectorVarNames) {
            List<RealVector> var = learningModel.getVectorVarByName(name);
            List<RealVector> grad = vectorGrads.get(name);
            for (int i=0; i<var.size(); i++) {
                learningModel.setVectorVarByNameIndex(name,
                                                      i,
                                                      var.get(i).combineToSelf(1.0, -1.0,
                                                                               grad.get(i).mapMultiplyToSelf(lr)));
            }
        }
    }

    public double minimize(LearningModel model, LearningData learningData, LearningData validData) {
        ObjectiveFunction objFunc = model.getObjectiveFunction();
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        ObjectiveTerminationCriterion validCrit = null;
        if (validData != null) {
            validCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        }
        assignGrads(model);
        L2Regularizer l2term = new L2Regularizer();
        double objVal = 0;
        while (termCrit.keepIterate()) {
            if (validCrit != null && !(validCrit.keepIterate())) {
                break;
            }
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
                    int idx = orc.scalarIndexes.getInt(i);
                    double grad = orc.scalarGrads.getDouble(i);
                    double var = model.getScalarVarByNameIndex(name, idx);
                    scalarGrads.get(name).addToEntry(idx, grad + l2coef * l2term.getGradient(var));
                }
                for (int i=0; i<orc.vectorNames.size(); i++) {
                    String name = orc.vectorNames.get(i);
                    int idx = orc.vectorIndexes.getInt(i);
                    RealVector var = model.getVectorVarByNameIndex(name, idx);
                    RealVector grad = orc.vectorGrads.get(i);
                    vectorGrads.get(name).get(idx).combineToSelf(1.0, 1.0, l2term.addGradient(grad, var, l2coef));
                }
                objVal += orc.objVal;
            }
            updateVars(model);
            termCrit.addIteration(BatchGradientDescent.class.toString() + " -- Learning", objVal);

            if (validData != null) {
                double validObjVal = evaluate(model, validData);
                validCrit.addIteration(BatchGradientDescent.class.toString()
                                               + " -- Validating", validObjVal);
            }
        }
        return objVal;
    }
}
