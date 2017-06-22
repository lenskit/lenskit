package org.lenskit.pf;


import org.lenskit.inject.Shareable;

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
