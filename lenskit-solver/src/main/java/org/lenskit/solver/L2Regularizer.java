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
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class L2Regularizer {
    @Inject
    public L2Regularizer() {}

    public double getValue(double var) {
        return var * var;
    }

    public double getGradient(double var) {
        return 2 * var;
    }

    public RealVector addGradient(RealVector grad, RealVector var, double l2coef) {
        return grad.combineToSelf(1.0, 2 * l2coef, var);
    }

    public double getObjective(double l2coef, RealVector var) {
        double l2norm = var.getNorm();
        return l2coef * l2norm * l2norm;
    }

    public double getObjective(double l2coef, List<RealVector> vars) {
        double objVal = 0.0;
        for (RealVector realVector : vars) {
            double l2norm = realVector.getNorm();
            objVal += l2norm * l2norm;
        }
        return objVal * l2coef;
    }
}
