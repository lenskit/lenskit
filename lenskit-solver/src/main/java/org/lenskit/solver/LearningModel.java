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

import java.io.Serializable;
import java.util.List;

/**
 * An interface for learning model in order to be optimized by {@link OptimizationMethod}. It is usually backed up
 * by a {@link org.lenskit.space.VariableSpace}.
 * See {@link AbstractLearningModel} for a boil-plate implementation.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface LearningModel extends Serializable {
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

    /**
     * Make prediction on the learning instance ins.
     * @return a real valued prediction, e.g. predicted rating or probability.
     */
    double predict(LearningInstance ins);

    /**
     * A learning model takes in an instance and construct a stochastic oracle, i.e. stochastic gradient for the current
     * instance, including the label, model prediction and the weight of the instance.
     * @return
     */
    StochasticOracle getStochasticOracle(LearningInstance ins);

    /**
     * @return the objective function of the learning model which is specified by the users of LearningModel.
     */
    ObjectiveFunction getObjectiveFunction();
}
