/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
 * <p>
 * An implementation of a moving average.<br/>
 * Only the sum and the item count is stored, not the values them self.
 * </p>
 * 
 * @author Matthias.Balke <matthias.balke@tu-dortmund.de>
 * @since 0.11
 * 
 */
public class AverageAccumulator {

    private double sum;
    private long count;

    public AverageAccumulator() {
        count = 0;
        sum = 0;
    }

    /**
     * Initialize the {@link AverageAccumulator} with a pre-calculated sum and
     * the amount of values included.
     * 
     * @param sum pre-calculated sum
     * @param count amount of values that where used to build this sum
     * 
     */
    public AverageAccumulator(double sum, long count) {
        this.count = count;
        this.sum = sum;
    }

    /**
     * Add a new datum to the {@link AverageAccumulator}
     * 
     * @param datum new datum to include into the average.
     */
    public void add(double datum) {
        sum += datum;
        count++;
    }

    /**
     * 
     * @return average over all added datums
     */
    public double getAverage() {
        if (count == 0 || sum == 0) {
            return 0;
        }

        return sum / count;
    }

    /**
     * @return amount of values this average is based on
     */
    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return Double.toString(this.getAverage());
    }
}
