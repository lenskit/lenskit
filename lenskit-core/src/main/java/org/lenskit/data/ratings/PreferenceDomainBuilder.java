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
package org.lenskit.data.ratings;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;

/**
 * Build a {@link PreferenceDomain}.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PreferenceDomainBuilder implements Builder<PreferenceDomain> {
    private double minimum = Double.NaN;
    private double maximum = Double.NaN;
    private double precision = Double.NaN;

    /**
     * Create an uninitialized preference domain builder.  The minimum and maximum must be provided
     * before the {@link #build()} method may be called.
     */
    public PreferenceDomainBuilder() {}

    /**
     * Create a preference domain builder with a specified minimum and maximum.
     * @param min The minimum preference.
     * @param max The maximum preference.
     */
    public PreferenceDomainBuilder(double min, double max) {
        setMinimum(min);
        setMaximum(max);
    }

    /**
     * Get the minimum preference.
     * @return The minimum preference.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * Set the minimum preference.
     * @param min The minimum preference.
     * @return The builder (for chaining).
     */
    public PreferenceDomainBuilder setMinimum(double min) {
        minimum = min;
        return this;
    }

    /**
     * Get the maximum preference.
     * @return The maximum preference.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Set the maximum preference.
     * @param max The maximum preference.
     * @return The builder (for chaining).
     */
    public PreferenceDomainBuilder setMaximum(double max) {
        maximum = max;
        return this;
    }

    /**
     * Get the preference precision.
     * @return The preference precision.
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Set the preference precision.
     * @param prec The preference precision, or {@link Double#NaN} for unlimited precision.
     * @return The preference precision.
     */
    public PreferenceDomainBuilder setPrecision(double prec) {
        precision = prec;
        return this;
    }

    @Override
    public PreferenceDomain build() {
        Preconditions.checkState(!Double.isNaN(minimum), "no minimum preference specified");
        Preconditions.checkState(!Double.isNaN(maximum), "no maximum preference specified");
        return new PreferenceDomain(minimum, maximum, precision);
    }
}
