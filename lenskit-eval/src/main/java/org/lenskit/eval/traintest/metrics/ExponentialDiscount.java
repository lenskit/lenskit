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
