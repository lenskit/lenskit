package org.lenskit.pf;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.inject.Transient;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Random;

public class RandomInitializationStrategyProvider implements Provider<InitializationStrategy> {

    private final PFHyperParameters hyperParameters;
    private final int userNum;
    private final int itemNum;
    private final double maxOffsetShp;
    private final double maxOffsetRte;
    private final long rndSeed;

    @Inject
    public RandomInitializationStrategyProvider(@Transient PFHyperParameters hyperParams,
                                                @Transient DataSplitStrategy data,
                                                @MaxRandomOffsetForShape double maxOffS,
                                                @MaxRandomOffsetForRate double mOffR,
                                                @RandomSeed long seed) {
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
            }
            double value = dPrime + maxOffsetRte*random.nextDouble();
            tauRte.setEntry(i, value);
            tauShp.setEntry(i, tShp);
        }

        return new RandomInitializationStrategy(gammaShp, gammaRte, kappaShp, kappaRte, lambdaShp, lambdaRte, tauShp, tauRte);
    }
}
