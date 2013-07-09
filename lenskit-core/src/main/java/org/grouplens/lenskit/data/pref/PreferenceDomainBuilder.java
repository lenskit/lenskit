/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.pref;

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
