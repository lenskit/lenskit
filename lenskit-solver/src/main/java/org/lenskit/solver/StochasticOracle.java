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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

/**
 * The data structure carrying oracle information (i.e. gradients) for optimization methods in oracle-based framework,
 * which connects optimization methods and learning models.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class StochasticOracle {
    List<String> scalarNames;
    IntArrayList scalarIndexes;
    DoubleArrayList scalarGrads;
    List<String> vectorNames;
    IntArrayList vectorIndexes;
    List<RealVector> vectorGrads;

    double objVal;
    double modelOutput;
    double insLabel;
    double insWeight;

    public StochasticOracle() {
        scalarNames = new ArrayList<>();
        scalarIndexes = new IntArrayList();
        scalarGrads = new DoubleArrayList();
        vectorNames = new ArrayList<>();
        vectorIndexes = new IntArrayList();
        vectorGrads = new ArrayList<>();
    }

    public void addScalarOracle(String name, int index, double grad) {
        scalarIndexes.add(index);
        scalarNames.add(name);
        scalarGrads.add(grad);
    }

    public void addVectorOracle(String name, int index, RealVector grad) {
        vectorIndexes.add(index);
        vectorNames.add(name);
        vectorGrads.add(grad);
    }

    public void setModelOutput(double modelOutput) {
        this.modelOutput = modelOutput;
    }

    public void setInsLabel(double insLabel) {
        this.insLabel = insLabel;
    }

    public void setInsWeight(double insWeight) {
        this.insWeight = insWeight;
    }
}
