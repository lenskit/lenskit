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

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LearningOracle {
    private double objval;
    private double modelOutput;
    private double insLabel;
    private DoubleArrayList variables;
    private DoubleArrayList gradients;
    private IntArrayList varIndexes;

    public LearningOracle() {
        objval = 0;
        modelOutput = 0;
        variables = new DoubleArrayList();
        gradients = new DoubleArrayList();
        varIndexes = new IntArrayList();
    }

    public double getModelOutput() {
        return modelOutput;
    }

    public void setModelOutput(double output) {
        modelOutput = output;
    }

    public double getInstanceLabel() {
        return insLabel;
    }

    public void setInstanceLabel(double label) {
        insLabel = label;
    }

    public double getObjValue() {
        return objval;
    }

    public void setObjValue(double newVal) {
        objval = newVal;
    }

    public DoubleArrayList getVariables() {
        return variables;
    }

    public DoubleArrayList getGradients() {
        return gradients;
    }

    public IntArrayList getVarIndexes() {
        return varIndexes;
    }

    public void addVariable(double var) {
        variables.add(var);
    }

    public void addGradient(double grad) {
        gradients.add(grad);
    }

    public void addVarIndex(int varIndex) {
        varIndexes.add(varIndex);
    }

    public void setVariable(int index, double var) {
        variables.set(index, var);
    }

    public void setGradient(int index, double grad) {
        gradients.set(index, grad);
    }

    public void setVarIndex(int index, int varInd) {
        varIndexes.set(index, varInd);
    }
}
