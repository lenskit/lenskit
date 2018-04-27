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
