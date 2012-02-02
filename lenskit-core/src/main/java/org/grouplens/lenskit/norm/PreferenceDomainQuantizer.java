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
package org.grouplens.lenskit.norm;

import org.grouplens.lenskit.data.pref.PreferenceDomain;

/**
 * Quantizer that uses a range and precision to determine discrete values.
 * Values are rounded to the closest discrete value.
 * @author Michael Ekstrand
 */
public class PreferenceDomainQuantizer extends ValueArrayQuantizer {
    private final PreferenceDomain domain;

    static double[] makeValues(PreferenceDomain domain) {
        if (!domain.hasPrecision()) {
            throw new IllegalArgumentException("domain is not discrete");
        }
        final double min = domain.getMinimum();
        final double max = domain.getMaximum();
        final double prec = domain.getPrecision();
        final double nv = (max - min) / prec;
        int n = (int) nv;
        if (Math.abs(nv - n) > 1.0e-6) {
            n += 1; // one more to cover everything...
        }
        if (n == 0) {
            throw new IllegalArgumentException("range has no elements");
        }
        double[] values = new double[n+1];
        for (int i = 0; i <= n; i++) {
            values[i] = min + (prec*i);
        }
        return values;
    }

    /**
     * Create a new quantizer from a discrete preference domain.
     * @param dom The preference domain.
     * @throws IllegalArgumentException if the domain is not discrete.
     */
    public PreferenceDomainQuantizer(PreferenceDomain dom) {
        super(makeValues(dom));
        domain = dom;
    }

    /**
     * Create a new preference domain quantizer.
     * 
     * @see PreferenceDomain#PreferenceDomain(double, double, double)
     */
    public PreferenceDomainQuantizer(double min, double max, double prec) {
        this(new PreferenceDomain(min, max, prec));
    }

    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }
}
