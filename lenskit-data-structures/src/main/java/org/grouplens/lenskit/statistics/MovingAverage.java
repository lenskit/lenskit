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
 * A simple implementation of a moving average.<br/>
 * Only the average value and the item count is stored, not the values it self.
 * This is ideal, for saving memory on large data sets.
 * </p>
 * <p>
 * The removal of values is also supported. But be careful not to remove values
 * that have never been added. Most of the time this will lead to unwanted
 * results!
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

    public MovingAverage(long count, double average) {
        this.count = count;
        this.average = average;
    }

    public void add(double value) {
        if (count == 0) {
            average = value;
            count++;
        } else {
            average = ((average * count) + value) / ++count;
        }
    }

    public void remove(double value) {
        if (count == 0) {
            throw new AssertionError("no value to remove");
        } else {
            average = ((average * count) - value) / --count;
        }
    }

    public double getAverage() {
        return average;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return Double.toString(average);
    }
}
