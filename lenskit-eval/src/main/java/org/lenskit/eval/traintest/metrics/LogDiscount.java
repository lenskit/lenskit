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
package org.lenskit.eval.traintest.metrics;

import com.google.common.base.Preconditions;

/**
 * Logarithmic discounting.  All ranks up through the log base have a discount of 1.
 */
public class LogDiscount implements Discount {
    private final double logBase;
    private final double logScaleTerm;

    /**
     * Construct a log discount.
     * @param base The discount base.
     */
    public LogDiscount(double base) {
        Preconditions.checkArgument(base > 1, "base must be greater than 1");
        logBase = base;
        logScaleTerm = Math.log(base);
    }

    /**
     * Get the log base from the discount.
     * @return The discount's log base.
     */
    public double getLogBase() {
        return logBase;
    }

    @Override
    public double discount(int rank) {
        if (rank < logBase) {
            return 1;
        } else {
            return logScaleTerm / Math.log(rank);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogDiscount that = (LogDiscount) o;

        return Double.compare(that.logBase, logBase) == 0;

    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(logBase);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return "LogDiscount(" + logBase + ")";
    }
}
