package org.lenskit.pf;


import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

@DefaultProvider(RandomInitializationStrategyProvider.class)
@Shareable
public class RandomInitializationStrategy implements Serializable, InitializationStrategy {
    private static final long serialVersionUID = 3L;

    private final RealMatrix gammaShp;
    private final RealMatrix gammaRte;
    private final RealVector kappaShp;
    private final RealVector kappaRte;
    private final RealMatrix lambdaShp;
    private final RealMatrix lambdaRte;
    private final RealVector tauShp;
    private final RealVector tauRte;

    public RandomInitializationStrategy(RealMatrix gShp, RealMatrix gRte, RealVector kShp, RealVector kRte,
                                        RealMatrix lShp, RealMatrix lRte, RealVector tShp, RealVector tRte) {
        gammaShp = gShp;
        gammaRte = gRte;
        kappaShp = kShp;
        kappaRte = kRte;
        lambdaShp = lShp;
        lambdaRte = lRte;
        tauShp = tShp;
        tauRte = tRte;

    }

    @Override
    public RealMatrix getGammaShp() {
        return gammaShp;
    }

    @Override
    public RealMatrix getGammaRte() {
        return gammaRte;
    }

    @Override
    public RealVector getKappaShp() {
        return kappaShp;
    }

    @Override
    public RealVector getKappaRte() {
        return kappaRte;
    }

    @Override
    public RealMatrix getLambdaShp() {
        return lambdaShp;
    }

    @Override
    public RealMatrix getLambdaRte() {
        return lambdaRte;
    }

    @Override
    public RealVector getTauShp() {
        return tauShp;
    }

    @Override
    public RealVector getTauRte() {
        return tauRte;
    }
}
