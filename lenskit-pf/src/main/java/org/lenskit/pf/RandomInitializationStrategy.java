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

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;

import java.io.Serializable;

/**
 * Random initialization of variable parameters
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
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
