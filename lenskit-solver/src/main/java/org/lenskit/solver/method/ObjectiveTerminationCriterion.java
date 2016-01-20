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

import java.util.ArrayList;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ObjectiveTerminationCriterion {
    private int maxIter;
    private int curIter;
    private double tol;
    private ArrayList<Double> objHistory;

    public ObjectiveTerminationCriterion(double outTol, int outMaxIter) {
        maxIter = outMaxIter;
        tol = outTol;
        curIter = 0;
        objHistory = new ArrayList<Double>();
    }

    public void addIteration(double objval) {
        curIter++;
        objHistory.add(objval);
        System.out.println(Integer.toString(curIter) + ": " + Double.toString(objval));
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
