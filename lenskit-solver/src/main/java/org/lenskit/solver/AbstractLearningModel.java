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
import org.lenskit.space.IndexSpace;
import org.lenskit.space.SynchronizedIndexSpace;
import org.lenskit.space.SynchronizedVariableSpace;
import org.lenskit.space.VariableSpace;

import java.io.Serializable;
import java.util.List;

abstract public class AbstractLearningModel implements LearningModel {
    final protected VariableSpace variableSpace = new SynchronizedVariableSpace();
    final protected IndexSpace indexSpace = new SynchronizedIndexSpace();

    protected AbstractLearningModel() {}

    public RealVector getScalarVarByName(String name) {
        return variableSpace.getScalarVarByName(name);
    }

    public int getScalarVarSizeByName(String name) {
        return variableSpace.getScalarVarSizeByName(name);
    }

    public void setScalarVarByName(String name, RealVector vars) {
        variableSpace.setScalarVarByName(name, vars);
    }

    public double getScalarVarByNameIndex(String name, int index) {
        return variableSpace.getScalarVarByNameIndex(name, index);
    }

    public void setScalarVarByNameIndex(String name, int index, double var) {
        variableSpace.setScalarVarByNameIndex(name, index, var);
    }

    public List<RealVector> getVectorVarByName(String name) {
        return variableSpace.getVectorVarByName(name);
    }

    public int getVectorVarSizeByName(String name) {
        return variableSpace.getVectorVarSizeByName(name);
    }

    public int getVectorVarDimensionByName(String name) {
        return variableSpace.getVectorVarDimensionByName(name);
    }

    public RealVector getVectorVarByNameIndex(String name, int index) {
        return variableSpace.getVectorVarByNameIndex(name, index);
    }

    public void setVectorVarByNameIndex(String name, int index, RealVector var) {
        variableSpace.setVectorVarByNameIndex(name, index, var);
    }

    public List<String> getAllScalarVarNames() {
        return variableSpace.getAllScalarVarNames();
    }

    public List<String> getAllVectorVarNames() {
        return variableSpace.getAllVectorVarNames();
    }
}
