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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.inject.Transient;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Random;

/**
 * A provider for {@link RandomInitializationStrategy}
 * Initialize the user parameters and item parameters to the prior with a small random offset
 * Set the user activity and item popularity shape parameters with
 * k_u_shp = a' + Ka; tau_i_shp = c' + Kc
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RandomInitializationStrategyProvider implements Provider<InitializationStrategy> {

    private final PFHyperParameters hyperParameters;
    private final int userNum;
    private final int itemNum;
    private final double maxOffsetShp;
    private final double maxOffsetRte;
    private final long rndSeed;

    @Inject
    public RandomInitializationStrategyProvider(PFHyperParameters hyperParams,
                                                @Transient DataSplitStrategy data,
                                                @MaxRandomOffsetForShape double maxOffS,
                                                @MaxRandomOffsetForRate double mOffR,
                                                @RandomSeed int seed) {
        hyperParameters = hyperParams;
        userNum = data.getUserIndex().size();
        itemNum = data.getItemIndex().size();
        maxOffsetShp = maxOffS;
        maxOffsetRte = mOffR;
        rndSeed = seed;
    }

    @Override
    public RandomInitializationStrategy get() {

        final int featureCount = hyperParameters.getFeatureCount();
        final double a = hyperParameters.getA();
        final double aPrime = hyperParameters.getAPrime();
        final double bPrime = hyperParameters.getBPrime();
        final double c = hyperParameters.getC();
        final double cPrime = hyperParameters.getCPrime();
        final double dPrime = hyperParameters.getDPrime();

        RealMatrix gammaShp = MatrixUtils.createRealMatrix(userNum, featureCount);
        RealMatrix gammaRte = MatrixUtils.createRealMatrix(userNum, featureCount);
        RealVector kappaShp = MatrixUtils.createRealVector(new double[userNum]);
        RealVector kappaRte = MatrixUtils.createRealVector(new double[userNum]);
        RealMatrix lambdaShp = MatrixUtils.createRealMatrix(itemNum, featureCount);
        RealMatrix lambdaRte = MatrixUtils.createRealMatrix(itemNum, featureCount);
        RealVector tauShp = MatrixUtils.createRealVector(new double[itemNum]);
        RealVector tauRte = MatrixUtils.createRealVector(new double[itemNum]);

        final Random random = new Random(rndSeed);
        double kShp = aPrime + featureCount * a;
        double tShp = cPrime + featureCount * c;

        for (int u = 0; u < userNum; u++ ) {
            for (int k = 0; k < featureCount; k++) {
                double valueShp = a + maxOffsetShp*random.nextDouble();
                double valueRte = bPrime + maxOffsetRte*random.nextDouble(); //not sure
                gammaShp.setEntry(u, k, valueShp);
                gammaRte.setEntry(u, k, valueRte);
                valueRte = gammaRte.getEntry(0, k);// make rate parameter have
                gammaRte.setEntry(u, k, valueRte); // same initials cross user delete these two line
            }

            double value = bPrime + maxOffsetRte*random.nextDouble();
            kappaRte.setEntry(u, value);
            kappaShp.setEntry(u, kShp);
        }

        for (int i = 0; i < itemNum; i++ ) {
            for (int k = 0; k < featureCount; k++) {
                double valueShp = c + maxOffsetShp*random.nextDouble();
                double valueRte = dPrime + maxOffsetRte*random.nextDouble(); //not sure
                lambdaShp.setEntry(i, k, valueShp);
                lambdaRte.setEntry(i, k, valueRte);
                valueRte = lambdaRte.getEntry(0, k); // make rate parameter have
                lambdaRte.setEntry(i, k, valueRte); // same initials cross user delete these two line
            }
            double value = dPrime + maxOffsetRte*random.nextDouble();
            tauRte.setEntry(i, value);
            tauShp.setEntry(i, tShp);
        }

        return new RandomInitializationStrategy(gammaShp, gammaRte, kappaShp, kappaRte, lambdaShp, lambdaRte, tauShp, tauRte);
    }
}
