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

import org.apache.commons.math3.linear.RealVector;

import javax.inject.Inject;
import java.util.List;

/**
 * A general online optimization solver, stochastic gradient descent algorithm.
 * Objective function is changed from f(X) to f(X) + l2coef * |X|^2
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public class StochasticGradientDescent extends AbstractOnlineOptimizationMethod {
    private double l2coef;
    private double lr;

    @Inject
    public StochasticGradientDescent() {
        super();
        maxIter = 60;
        l2coef = 0.1;
        lr = 0.01;
        tol = 5.0;
    }

    public StochasticGradientDescent(int maxIter, double l2coef, double learningRate, double tol) {
        super();
        this.maxIter = maxIter;
        this.l2coef = l2coef;
        this.lr = learningRate;
        this.tol = tol;
    }

    public double update(LearningModel model, LearningData learningData) {
        ObjectiveFunction objFunc = model.getObjectiveFunction();
        L2Regularizer l2term = new L2Regularizer();
        double objVal = 0.0;
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
        LearningInstance ins;
        while ((ins = learningData.getLearningInstance()) != null) {
            StochasticOracle orc = model.getStochasticOracle(ins);
            objFunc.wrapOracle(orc);
            for (int i=0; i<orc.scalarNames.size(); i++) {
                String name = orc.scalarNames.get(i);
                int idx = orc.scalarIndexes.getInt(i);
                double grad = orc.scalarGrads.getDouble(i);
                double var = model.getScalarVarByNameIndex(name, idx);
                model.setScalarVarByNameIndex(name, idx, var - lr * (grad + l2coef * l2term.getGradient(var)));
            }
            for (int i=0; i<orc.vectorNames.size(); i++) {
                String name = orc.vectorNames.get(i);
                int idx = orc.vectorIndexes.getInt(i);
                RealVector var = model.getVectorVarByNameIndex(name, idx);
                RealVector grad = orc.vectorGrads.get(i);
                model.setVectorVarByNameIndex(name, idx, var.combineToSelf(1.0, -lr,
                                                                           l2term.addGradient(grad, var, l2coef)));
            }
            objVal += orc.objVal;
        }
        return objVal;
    }
}
