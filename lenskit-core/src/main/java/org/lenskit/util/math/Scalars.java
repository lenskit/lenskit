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

public final class Scalars {
    /**
     * The default epsilon for checking for zero.  If the Java system property {@code lenskit.zero.epsilon} is set,
     * the value of that is used; otherwise, {@link Double#MIN_NORMAL}.
     */
    public static final double DEFAULT_EPSILON;
    private static final double LOG2_ADJ = 1.0 / Math.log(2);

    private Scalars() {}

    static {
        String prop = System.getProperty("lenskit.zero.epsilon");
        if (prop == null) {
            DEFAULT_EPSILON = Double.MIN_NORMAL;
        } else {
            DEFAULT_EPSILON = Double.parseDouble(prop);
        }
    }

    /**
     * Check whether a value is zero, using a default epsilon.
     * @param val The value.
     * @return {@code true} if the value is within an epsilon of zero.
     */
    public static boolean isZero(double val) {
        return isZero(val, DEFAULT_EPSILON);
    }

    /**
     * Check whether a value is zero, using a configurable epsilon.
     * @param val The value.
     * @param epsilon The tolerance.
     * @return {@code true} if absolute value of {@code val} is less than {@code epsilon}.
     */
    public static boolean isZero(double val, double epsilon) {
        return Math.abs(val) < epsilon;
    }

    public static double log2(double x) {
        return Math.log(x) * LOG2_ADJ;
    }
}
