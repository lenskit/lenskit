/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.opt;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.io.IOException;

import org.grouplens.lenskit.obj.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to <f'(X), X> + l1coef * |X| + l2coef * |X|^2
//                                                       + 1/(2*learningRate) * |X - Xt|^2 
public class CompositeObjectiveGradientDescent extends OptimizationHelper implements OptimizationMethod {

    //l1coef should be greater than zero, since it's meaningless to use this method with l1coef 0;
    //       with l1coef 0, it's exactly Stochastic Gradient Descent
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
                DoubleArrayList varList = orc.getVariables();
                DoubleArrayList gradList = orc.getGradients();
                for (int i=0; i<varList.size(); i++) {
                    double var = varList.get(i);
                    double grad = gradList.get(i);
                    grad += getL2RegularizerGradient(var, l2coef);
                    double signDecider = var / learningRate - grad;
                    double newVar = var - learningRate * (grad + Math.signum(var) * l1coef);
                    if (signDecider * newVar < 0) {
                        newVar = 0;
                    }
                    model.setVariable(i, newVar);
                }
                objval += orc.getObjValue();
            }
            termCrit.addIteration(objval);
        }
    }
}
