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
 * Exponential (half-life) discounting.
 */
public class ExponentialDiscount implements Discount {
    private final double alpha;

    /**
     * Construct an exponential discount.
     * @param hl The half-life.
     */
    public ExponentialDiscount(double hl) {
        Preconditions.checkArgument(hl > 1, "half-life must be greater than 1");
        alpha = hl;
    }

    /**
     * Get the half-life of the discount function.
     * @return The half-life of the discount function.
     */
    public double getHalfLife() {
        return alpha;
    }

    @Override
    public double discount(int rank) {
        return 1 / Math.pow(2, (rank - 1) / (alpha - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExponentialDiscount that = (ExponentialDiscount) o;

        return Double.compare(that.alpha, alpha) == 0;

    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(alpha);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return "ExponentialDiscount(" + alpha + ")";
    }
}
