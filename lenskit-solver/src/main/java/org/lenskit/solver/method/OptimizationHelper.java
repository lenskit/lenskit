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

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class OptimizationHelper {
    private L1Regularizer l1term;
    private L2Regularizer l2term;

    public OptimizationHelper() {
        l1term = new L1Regularizer();
        l2term = new L2Regularizer();
    }

    protected double getL2RegularizerGradient(double var, double l2coef) {
        return l2coef * l2term.getGradient(var);
    }

    protected double getRegularizerGradient(double var, double l1coef, double l2coef) {
        double grad = 0;
        if (l1coef > 0) {
            grad += l1coef * l1term.getSubGradient(var);
        }
        if (l2coef > 0) {
            grad += l2coef * l2term.getGradient(var);
        }
        return grad;
    }

    protected double getRegularizerObjective(LearningModel model, double l1coef, double l2coef) {
        double objval = 0;
        int numVars = model.getNumOfVariables();
        if (l1coef > 0 || l2coef > 0) {
            for (int i=0; i<numVars; i++) {
                double var = model.getVariable(i);
                if (l1coef > 0) {
                    objval += l1coef * l1term.getValue(var);
                }
                if (l2coef > 0) {
                    objval += l2coef * l2term.getValue(var);
                }
            }
        }
        return objval;
    }
}
