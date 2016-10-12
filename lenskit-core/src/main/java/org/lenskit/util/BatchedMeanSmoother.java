/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util;

/**
 * A smoothing class that computes rolling, windowed means of batches of items.
 */
class BatchedMeanSmoother {
    private final int windowCount;
    private final int[] batchSizes;
    private final double[] batchValues;
    private double total;
    private int totalCount;
    private int pos = 0;

    /**
     * Create a new batch mean smoother.
     * @param w The number of batches over which the mean should be computed.  If 0, then global means are computed.
     */
    public BatchedMeanSmoother(int w) {
        windowCount = w;
        if (w > 0) {
            batchSizes = new int[w];
            batchValues = new double[w];
        } else {
            batchSizes = null;
            batchValues = null;
        }
    }

    public double currentAverage() {
        return total / totalCount;
    }

    /**
     * Add a new batch of items.
     * @param n The number of items.
     * @param val The total value across items.
     */
    public void addBatch(int n, double val) {
        if (windowCount > 0) {
            assert batchValues != null;
            assert batchSizes != null;
            total -= batchValues[pos];
            totalCount -= batchSizes[pos];
            batchValues[pos] = val;
            batchSizes[pos] = n;
            pos = (pos + 1) % windowCount;
        }
        total += val;
        totalCount += n;
    }
}
