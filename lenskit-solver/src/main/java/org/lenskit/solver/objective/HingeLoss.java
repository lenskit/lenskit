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
package org.grouplens.lenskit.obj;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import org.grouplens.lenskit.opt.LearningOracle;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HingeLoss implements ObjectiveFunction {
    public HingeLoss() { }

    public void wrapOracle(LearningOracle orc) {
        double output = orc.getModelOutput();
        double label = orc.getInstanceLabel();
        if (label == 0) {
            label = -1;
        }
        double loss = 1 - output * label;
        loss = (loss < 0) ? 0 : loss;
        orc.setObjValue(loss);
        DoubleArrayList gradList = orc.getGradients();
        double hingeGrad = (loss == 0) ? 0 : -label;
        for (int i=0; i<gradList.size(); i++) {
            orc.setGradient(i, hingeGrad * gradList.get(i));
        }
    }
}
