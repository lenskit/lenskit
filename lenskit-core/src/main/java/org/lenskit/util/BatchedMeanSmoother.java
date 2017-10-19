/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
