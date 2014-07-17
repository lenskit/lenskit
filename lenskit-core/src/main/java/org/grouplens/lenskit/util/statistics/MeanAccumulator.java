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
package org.grouplens.lenskit.util.statistics;

/**
 * Accumulate a mean.
 * Only the sum and the item count is stored, not the values themselves.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
public class MeanAccumulator {

    private double accSum;
    private long accCount;

    /**
     * Construct a new, zeroed average accumulator.
     */
    public MeanAccumulator() {
        accCount = 0;
        accSum = 0;
    }

    /**
     * Initialize the {@link MeanAccumulator} with a pre-calculated sum and
     * the amount of values included.
     *
     * @param sum   pre-calculated sum
     * @param count amount of values that where used to build this sum
     */
    public MeanAccumulator(double sum, long count) {
        accCount = count;
        accSum = sum;
    }

    /**
     * Add a new datum to the {@link MeanAccumulator}.
     *
     * @param datum new datum to include into the average.
     */
    public void add(double datum) {
        accSum += datum;
        accCount++;
    }

    /**
     * @return average over all added datums
     */
    public double getMean() {
        if (accCount == 0 || accSum == 0) {
            return 0;
        }

        return accSum / accCount;
    }

    /**
     * Get the total of the values accumulated so far.
     * @return The total of the values.
     */
    public double getTotal() {
        return accSum;
    }

    /**
     * @return amount of values this average is based on
     */
    public long getCount() {
        return accCount;
    }

    @Override
    public String toString() {
        return Double.toString(this.getMean());
    }
}
