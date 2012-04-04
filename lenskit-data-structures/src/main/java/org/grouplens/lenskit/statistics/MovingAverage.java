/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.statistics;

/**
 * <p>
 * An implementation of a moving average.<br/>
 * Only the average value and the item count is stored, not the values them
 * self.
 * </p>
 * 
 * @author Matthias.Balke <matthias.balke@tu-dortmund.de>
 * @since 0.10
 * 
 */
public class MovingAverage {

    private double average;
    private long count;

    public MovingAverage() {
        count = 0;
        average = 0;
    }

    /**
     * initialize the {@link MovingAverage} with a pre-calculated average value.
     * 
     * @param count amount of values that where used to build this average
     * @param average pre-calculated average
     */
    public MovingAverage(long count, double average) {
        this.count = count;
        this.average = average;
    }

    /**
     * Add a new datum to the {@link MovingAverage}
     * 
     * @param datum new datum to include into the average.
     */
    public void add(double datum) {
        if (count == 0) {
            average = datum;
            count++;
        } else {
            average = ((average * count) + datum) / ++count;
        }
    }

    /**
     * 
     * @return average over all added datums
     */
    public double getAverage() {
        return average;
    }

    /**
     * @return amount of values this average is based on
     */
    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return Double.toString(average);
    }
}
