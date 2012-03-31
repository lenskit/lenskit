/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.params.MaxRating;
import org.grouplens.lenskit.params.MinRating;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object describing the domain of preference data, used in ratings and predictions.
 *
 * @review Should this be called RatingDomain?
 * @todo Integrate this with {@link MinRating} and {@link MaxRating}.
 * @author Michael Ekstrand
 */
public final class PreferenceDomain implements Serializable {
    public static final long serialVersionUID = 1L;

    private double minimum;
    private double maximum;
    private double precision;

    /**
     * Create a discrete bounded preference domain.
     * @param min The minimum preference value.
     * @param max The maximum preference value.
     * @param prec The preference precision (if {@link Double#NaN}, the domain
     *             is continuous).
     */
    public PreferenceDomain(double min, double max, double prec) {
        minimum = min;
        maximum = max;
        precision = prec;
    }

    /**
     * Create a continuous bounded preference domain.
     * @param min The minimum preference value.
     * @param max The maximum preference value.
     */
    public PreferenceDomain(double min, double max) {
        this(min, max, Double.NaN);
    }

    /**
     * The minimum preference value.
     * @return The minimum preference value.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * The maximum preference value.
     * @return The maximum preference value.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Query whether this preference domain has a precision.
     * @return {@code true} if the domain has a precision for discrete rating values, {@code false}
     *         if it is continuous.
     */
    public boolean hasPrecision() {
        return !Double.isNaN(precision);
    }

    /**
     * The precision of preference values. This is the precision with which data is
     * collected from the user — in a 1-5, half-star rating system, it will be 0.5.
     * @return The preference precision; the return value is undefined if the preference
     * domain has no precision.
     * @see #hasPrecision()
     */
    public double getPrecision() {
        if (hasPrecision()) {
            return precision;
        } else {
            return Double.MIN_VALUE;
        }
    }

    @Override
    public String toString() {
        String str = String.format("[%f,%f]", minimum, maximum);
        if (!Double.isNaN(precision)) {
            str += String.format("/%f", precision);
        }
        return str;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(minimum).append(maximum);
        if (hasPrecision()) {
            hcb.append(precision);
        }
        return hcb.toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof PreferenceDomain) {
            PreferenceDomain od = (PreferenceDomain) o;
            EqualsBuilder eqb = new EqualsBuilder();
            eqb.append(minimum, od.getMinimum());
            eqb.append(maximum, od.getMaximum());
            eqb.append(hasPrecision(), od.hasPrecision());
            if (eqb.isEquals() && hasPrecision() && od.hasPrecision()) {
                eqb.append(precision, od.getPrecision());
            }
            return eqb.isEquals();
        } else {
            return false;
        }
    }

    private static Pattern specRE =
            Pattern.compile("\\s*\\[\\s*((?:\\d*\\.)?\\d+)\\s*,\\s*((?:\\d*\\.)?\\d+)\\s*\\]\\s*(?:/\\s*((?:\\d*\\.)?\\d+))?\\s*");

    /**
     * Parse a preference domain from a string specification.
     * <p/>
     * Continuous preference domains are specified as {@code [min,max]}; discrete domains
     * as {@code min:max[/prec/}.  For example, a 0.5-5.0 half-star rating scale is represented
     * as {@code [0.5,5.0]/0.5}.
     * @param spec The string specifying the preference domain.
     * @return The preference domain represented by {@code spec}.
     * @throws IllegalArgumentException if {@code spec} is not a valid domain specification.
     */
    public static @Nonnull PreferenceDomain fromString(@Nonnull String spec) {
        Matcher m = specRE.matcher(spec);
        if (!m.matches()) {
            throw new IllegalArgumentException("invalid domain specification");
        }
        double min = Double.parseDouble(m.group(1));
        double max = Double.parseDouble(m.group(2));
        String precs = m.group(3);
        if (precs != null) {
            double prec = Double.parseDouble(precs);
            return new PreferenceDomain(min, max, prec);
        } else {
            return new PreferenceDomain(min, max);
        }
    }
}
