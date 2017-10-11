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
package org.lenskit.transform.quantize;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.inject.Shareable;
import org.lenskit.data.ratings.PreferenceDomain;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Quantizer that uses a range and precision to determine discrete values.
 * Values are rounded to the closest discrete value.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class PreferenceDomainQuantizer extends ValueArrayQuantizer {
    private static final long serialVersionUID = 2L;

    private final PreferenceDomain domain;

    static RealVector makeValues(PreferenceDomain domain) {
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
        double[] values = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            values[i] = min + (prec * i);
        }
        return new ArrayRealVector(values);
    }

    /**
     * Create a new quantizer from a discrete preference domain.
     *
     * @param dom The preference domain.
     * @throws IllegalArgumentException if the domain is not discrete.
     */
    @Inject
    public PreferenceDomainQuantizer(PreferenceDomain dom) {
        super(makeValues(dom));
        domain = dom;
    }

    /**
     * Create a new preference domain quantizer.
     *
     * @see PreferenceDomain#PreferenceDomain(double, double, double)
     */
    @SuppressWarnings("unused")
    public PreferenceDomainQuantizer(double min, double max, double prec) {
        this(new PreferenceDomain(min, max, prec));
    }

    @SuppressWarnings("unused")
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    public static class AutoProvider implements Provider<PreferenceDomainQuantizer> {
        private final PreferenceDomain domain;

        @Inject
        public AutoProvider(@Nullable PreferenceDomain dom) {
            domain = dom;
        }

        public PreferenceDomainQuantizer get() {
            if (domain == null) {
                return null;
            } else {
                return new PreferenceDomainQuantizer(domain);
            }
        }
    }
}
