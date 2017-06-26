/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.pf;

import org.lenskit.inject.Shareable;
import org.lenskit.mf.funksvd.FeatureCount;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * Hyper-parameters of poisson factorization
 *
 */
@Shareable
@Immutable
public final class PFHyperParameters implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double a;
    private final double aPrime;
    private final double bPrime;
    private final double c;
    private final double cPrime;
    private final double dPrime;
    private final int featureCount;


    @Inject
    public PFHyperParameters(@HyperParameterA double a,
                             @HyperParameterAPrime double aP,
                             @HyperParameterBPrime double bP,
                             @HyperParameterC double c,
                             @HyperParameterCPrime double cP,
                             @HyperParameterDPrime double dP,
                             @FeatureCount int k) {
        this.a = a;
        aPrime = aP;
        bPrime = bP;
        this.c = c;
        cPrime = cP;
        dPrime = dP;
        featureCount = k;
    }

    public double getA() {
        return a;
    }

    public double getAPrime() {
        return aPrime;
    }


    public double getBPrime() {
        return bPrime;
    }

    public double getC() {
        return c;
    }

    public double getCPrime() {
        return cPrime;
    }

    public double getDPrime() {
        return dPrime;
    }

    public int getFeatureCount() {
        return featureCount;
    }
}
