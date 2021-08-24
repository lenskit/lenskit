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
package org.lenskit.util.math;

import java.util.Arrays;

/**
 * Accumulates the mean of the most recent accumulated values. Meaning that its reported mean will only reflect the N
 * most recent values. This is accomplished using a fixed size array and a running total. Adding and getting the average
 * take O(1) time.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RollingWindowMeanAccumulator {
    private static final int DEFAULT_SIZE = 100;

    private final double[] data;
    // invariant - sum of data.
    private double sum;
    // invariant - if position is negative then the next index is data.length + position (and we are filling the data
    // array for the first time) otherwise position is the next index (meaning the index of the oldest value).
    private int position;

    /**
     * Construct a new, empty accumulator with default size.
     */
    public RollingWindowMeanAccumulator() {
        this(DEFAULT_SIZE);
    }

    /**
     * Construct a new, empty accumulator with default size.
     *
     * @param size the size of the rolling window.
     */
    public RollingWindowMeanAccumulator(int size) {
        data = new double[size];
        position = -size;
        sum = 0.0;
    }

    /**
     * Construct a new accumulator with specified size initially full with a given value
     * @param size the size of the rolling window.
     * @param value the initial value to fillw ith.
     */
    public RollingWindowMeanAccumulator(int size, double value) {
        data = new double[size];
        Arrays.fill(data, value);
        position = 0;
        sum = size*value;
    }

    /**
     * Add a new datum to the {@link RollingWindowMeanAccumulator}.
     *
     * @param datum new datum to include into the average.
     */
    public void add(double datum) {
        int index = position;
        if (index < 0) {
            index = data.length + index;
        }
        sum = sum - data[index] + datum;
        data[index] = datum;
        position = (position + 1) % data.length;
    }

    /**
     * @return average over the most recently added datum, or 0 initially.
     */
    public double getMean() {
        int size = getSize();
        if (size == 0) {
            return 0;
        }
        return sum/size;
    }

    /**
     * @return the number of values represented in the average, this will count up from 0 up until the window size
     * after which it will stay at the window size.
     */
    public int getSize() {
        int size = data.length;
        if (position < 0) {
            size = data.length + position;
        }
        return size;
    }

    @Override
    public String toString() {
        return Double.toString(this.getMean());
    }
}
