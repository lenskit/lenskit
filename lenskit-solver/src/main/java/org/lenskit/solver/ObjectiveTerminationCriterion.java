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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ObjectiveTerminationCriterion {
    private static Logger logger = LoggerFactory.getLogger(ObjectiveTerminationCriterion.class);
    private int maxIter;
    private int curIter;
    private double tol;
    private DoubleArrayList objHistory;

    public ObjectiveTerminationCriterion(double tol, int maxIter) {
        this.maxIter = maxIter;
        this.tol = tol;
        curIter = 0;
        objHistory = new DoubleArrayList();
    }

    public void addIteration(double objVal) {
        curIter++;
        objHistory.add(objVal);
        logger.info("Iteration {}: objective value is {}", curIter, objVal);
    }

    public void addIteration(String step, double objVal) {
        curIter++;
        objHistory.add(objVal);
        logger.info("{}, Iteration {}: objective value is {}", step, curIter, objVal);
    }

    public boolean keepIterate() {
        if (curIter < 2) {
            return true;
        } else if (curIter >= maxIter) {
            return false;
        } else if (objHistory.get(curIter - 2) - objHistory.get(curIter - 1) < tol) {
            return false;
        } else {
            return true;
        }
    }
}
