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
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.List;
import java.util.Random;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RandomInitializer {
    double multi;
    Random rand;

    public RandomInitializer() {
        multi = 0.001;
        rand = new Random();
    }

    public RandomInitializer(long seed, double multiplier) {
        multi = multiplier;
        rand = new Random(seed);
    }

    public void randInitVector(RealVector vec, boolean normalize) {
        int len = vec.getDimension();
        double sum = 0.0;
        for (int i=0; i<len; i++) {
            double val = rand.nextDouble() * multi;
            vec.setEntry(i, val);
            if (normalize) {
                sum += val;
            }
        }
        if (normalize) {
            vec.mapDivideToSelf(sum);
        }
    }

    public void randInitDoubleList(List<Double> doubleList, boolean normalize) {
        int size = doubleList.size();
        double sum = 0.0;
        for (int i=0; i<size; i++) {
            double val = rand.nextDouble() * multi;
            doubleList.set(i, val);
            if (normalize) {
                sum += val;
            }
        }
        if (normalize) {
            for (int i=0; i<size; i++) {
                doubleList.set(i, doubleList.get(i) / sum);
            }
        }
    }

    public void randInitMatrix(RealMatrix mat, boolean normalize) {
        int len = mat.getRowDimension();
        RealVector vec = MatrixUtils.createRealVector(new double[mat.getColumnDimension()]);
        for (int i=0; i<len; i++) {
            randInitVector(vec, normalize);
            mat.setRowVector(i, vec);
        }
    }
}
