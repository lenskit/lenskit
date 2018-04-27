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
