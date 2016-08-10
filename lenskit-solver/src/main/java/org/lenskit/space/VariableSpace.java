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

package org.lenskit.space;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;
import java.util.List;

/**
 * A general interface of variable space (i.e. parameter server) for supporting any learning models.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface VariableSpace extends Serializable {

    /**
     * Request some scalar variables/parameters from the variable space. This array of scalar variables has name as its
     * name and the length is size. If randomize is false, then the initial value of the variables will be initial.
     * If randomize is true, then the initial values will be randomly generated and will be normalized to sum to one
     * if normalize is true.
     */
    void requestScalarVar(String name, int size, double initial,
                                       boolean randomize, boolean normalize);

    /**
     * Ensure the name scalar variable array has at least size length. Same with
     * {@link #requestScalarVar(String, int, double, boolean, boolean)} for initial and randomize.
     */
    void ensureScalarVar(String name, int size, double initial, boolean randomize);

    /**
     * Request some vector variables/parameters from the variable space. This array of vector variables has name as its
     * name and the length is size and each vector has dimension dim. If randomize is false, then the initial value of
     * the variables will be initial. If randomize is true, then the initial values will be randomly generated and will
     * be normalized to sum to one for each vector if normalize is true.
     */
    void requestVectorVar(String name, int size, int dim, double initial,
                                       boolean randomize, boolean normalize);

    /**
     * Ensure the name vector variable array has at least size length. Same with
     * {@link #requestScalarVar(String, int, double, boolean, boolean)} for dim, initial, randomize and normalize.
     */
    void ensureVectorVar(String name, int size, int dim, double initial,
                                      boolean randomize, boolean normalize);

    RealVector getScalarVarByName(String name);
    int getScalarVarSizeByName(String name);
    void setScalarVarByName(String name, RealVector vars);
    double getScalarVarByNameIndex(String name, int index);
    void setScalarVarByNameIndex(String name, int index, double var);

    List<RealVector> getVectorVarByName(String name);
    int getVectorVarSizeByName(String name);
    int getVectorVarDimensionByName(String name);
    RealVector getVectorVarByNameIndex(String name, int index);
    void setVectorVarByNameIndex(String name, int index, RealVector var);

    List<String> getAllScalarVarNames();
    List<String> getAllVectorVarNames();
}
