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

import javax.inject.Inject;

public class LogisticLoss implements ObjectiveFunction {
    @Inject
    public LogisticLoss() { }

    //unused
    static public double sigmoid(double y) {
        if (y < -30.0) {
            return 0.001;
        } else if (y > 30.0) {
            return 0.999;
        } else {
            return 1.0 / (1.0 + Math.exp(-y));
        }
    }

    public void wrapOracle(StochasticOracle orc) {
        orc.objVal = orc.insWeight * (Math.log(1.0 + Math.exp(orc.modelOutput)) - orc.insLabel * orc.modelOutput);
        double err = 1.0 / (1.0 + Math.exp(-orc.modelOutput)) - orc.insLabel;
        if (orc.insWeight != 1.0) {
            err *= orc.insWeight;
        }
        for (int i=0; i<orc.scalarGrads.size(); i++) {
            orc.scalarGrads.set(i, orc.scalarGrads.getDouble(i) * err);
        }
        for (int i=0; i<orc.vectorGrads.size(); i++) {
            orc.vectorGrads.get(i).mapMultiplyToSelf(err);
        }
    }
}
