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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definitions of different discount functions.
 */
public final class Discounts {
    private Discounts() {}

    private static final Pattern LOG_PAT = Pattern.compile("log(?:\\((\\d+)\\))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXP_PAT = Pattern.compile("exp\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);

    /**
     * Create a log-base-2 discount.  The discount function is:
     *
     * \\[\\mathrm{disc}(i) =
     * \\begin{cases}
     * 1 & i \\le 2 \\\\
     * (\\mathrm{log}_{2} i)^{-1} & \\mathrm{else}
     * \\end{cases} \\]
     *
     * @return The discount.
     */
    public static LogDiscount log2() {
        return new LogDiscount(2);
    }

    /**
     * Create a new logarithmic discount.  The discount function is:
     *
     * \\[\\mathrm{disc}(i) =
     * \\begin{cases}
     * 1 & i \\le b \\\\
     * (\\mathrm{log}_{b} i)^{-1} & \\mathrm{else}
     * \\end{cases} \\]
     *
     * @param base The log base $b$.
     * @return The discount.
     */
    public static LogDiscount log(double base) {
        return new LogDiscount(base);
    }

    /**
     * Create a new exponential (half-life) discount.  The discount function is:
     *
     * \\[\\mathrm{disc}(i) = \\left(2^{\\frac{i-1}{\\alpha-1}}\\right)^{-1}\\]
     *
     * @param hl The half-life $\\alpha$ of the decay function.
     * @return The discount.
     */
    public static ExponentialDiscount exp(double hl) {
        return new ExponentialDiscount(hl);
    }

    /**
     * Parse a discount expression from a string.
     * @param disc The discount string.
     * @return The discount.
     */
    public static Discount parse(String disc) {
        if (disc.toLowerCase().equals("log2")) {
            return log2();
        }

        Matcher m = LOG_PAT.matcher(disc);
        if (m.matches()) {
            String grp = m.group(1);
            double base = grp != null ? Double.parseDouble(grp) : 2;
            return new LogDiscount(base);
        }

        m = EXP_PAT.matcher(disc);
        if (m.matches()) {
            double hl = Double.parseDouble(m.group(1));
            return new ExponentialDiscount(hl);
        }

        throw new IllegalArgumentException("invalid discount specification " + disc);
    }
}
