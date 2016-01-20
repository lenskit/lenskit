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
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.IOException;

import org.grouplens.lenskit.obj.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

// Objective function is changed from f(X) to f(X) + l1coef * |X| + l2coef * |X|^2
public class StochasticGradientDescent extends OptimizationHelper implements OptimizationMethod {

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
                IntArrayList varIndList = orc.getVarIndexes();
                DoubleArrayList varList = orc.getVariables();
                DoubleArrayList gradList = orc.getGradients();
                for (int i=0; i<varList.size(); i++) {
                    double var = varList.get(i);
                    double grad = gradList.get(i);
                    grad += getRegularizerGradient(var, l1coef, l2coef);
                    double newVar = var - learningRate * grad;
                    if (var * newVar < 0 && l1coef > 0) {
                        newVar = 0;
                    }
                    int ind = varIndList.get(i);
                    model.setVariable(ind, newVar);
                }
                objval += orc.getObjValue();
            }
            //Probably we don't need calculate this, because it is used for termination decision.
            //objval += getRegularizerObjective(model, l1coef, l2coef);
            //We only use f(X) to decide the termination of iteration.
            termCrit.addIteration(objval);
        }
    }
}
