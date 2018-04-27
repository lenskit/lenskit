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
package org.lenskit.pf;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.special.Gamma;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.StoppingThreshold;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.KeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Sequential Implementation of HPF recommender builder.
 *
 * <p>
 * This recommender builder constructs an hierarchical Poisson matrix factorization recommender(HPF)
 * using a mean-field variational inference algorithm. These are documented in
 * <a href="https://arxiv.org/abs/1311.1704">Original paper: Scalable Recommendation with Poisson Factorization</a>.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HPFModelProvider implements Provider<HPFModel> {
    private static Logger logger = LoggerFactory.getLogger(HPFModelProvider.class);

    private final DataSplitStrategy ratings;
    private final PFHyperParameters hyperParameters;
    private final int iterationFrequency;
    private final double maxOffsetShp;
    private final double maxOffsetRte;
    private final long rndSeed;
    private final boolean isProbPredition;
    private final double threshold;
    private final int maxIterCount;


    @Inject
    public HPFModelProvider(@Transient DataSplitStrategy rndRatings,
                            PFHyperParameters hyperParams,
                            @ConvergenceCheckFrequency int iterFreq,
                            @RandomSeed int seed,
                            @MaxRandomOffsetForShape double maxOffS,
                            @MaxRandomOffsetForRate double maxOffR,
                            @IsProbabilityPrediction boolean probPred,
                            @StoppingThreshold double threshld,
                            @IterationCount int maxIter) {

        ratings = rndRatings;
        hyperParameters = hyperParams;
        iterationFrequency = iterFreq;
        rndSeed = seed;
        maxOffsetShp = maxOffS;
        maxOffsetRte = maxOffR;
        isProbPredition = probPred;
        threshold = threshld;
        maxIterCount = maxIter;
    }

    @Override
    public HPFModel get() {

        final int userNum = ratings.getUserIndex().size();
        final int itemNum = ratings.getItemIndex().size();
        final int featureCount = hyperParameters.getFeatureCount();
        final double a = hyperParameters.getUserWeightShpPrior();
        final double aPrime = hyperParameters.getUserActivityShpPrior();
        final double bPrime = hyperParameters.getUserActivityPriorMean();
        final double c = hyperParameters.getItemWeightShpPrior();
        final double cPrime = hyperParameters.getItemActivityShpPrior();
        final double dPrime = hyperParameters.getItemActivityPriorMean();
        final double kappaShpU = aPrime + featureCount * a;
        final double tauShpI = cPrime + featureCount * c;

        RealMatrix gammaShp = MatrixUtils.createRealMatrix(userNum, featureCount);
        RealMatrix gammaRte = MatrixUtils.createRealMatrix(userNum, featureCount);
        RealVector kappaShp = MatrixUtils.createRealVector(new double[userNum]);
        RealVector kappaRte = MatrixUtils.createRealVector(new double[userNum]);
        RealMatrix lambdaShp = MatrixUtils.createRealMatrix(itemNum, featureCount);
        RealMatrix lambdaRte = MatrixUtils.createRealMatrix(itemNum, featureCount);
        RealVector tauShp = MatrixUtils.createRealVector(new double[itemNum]);
        RealVector tauRte = MatrixUtils.createRealVector(new double[itemNum]);
        RealMatrix gammaShpNext = MatrixUtils.createRealMatrix(userNum, featureCount);
        RealMatrix lambdaShpNext = MatrixUtils.createRealMatrix(itemNum, featureCount);
        gammaShpNext = gammaShpNext.scalarAdd(a);
        lambdaShpNext = lambdaShpNext.scalarAdd(c);
        RealVector phiUI = MatrixUtils.createRealVector(new double[featureCount]);

        initialize(gammaShp, gammaRte, kappaRte, kappaShp,
                lambdaShp, lambdaRte, tauRte, tauShp);
        logger.info("initialization finished");

        final List<RatingMatrixEntry> train = ratings.getTrainRatings();
        final List<RatingMatrixEntry> validation = ratings.getValidationRatings();
        double avgPLLPre = Double.MAX_VALUE;
        double avgPLLCurr = 0.0;
        double diffPLL = 1.0;
        int iterCount = 1;

        while (iterCount < maxIterCount && diffPLL > threshold) {

            // update phi
            Iterator<RatingMatrixEntry> allUIPairs = train.iterator();
            while (allUIPairs.hasNext()) {
                RatingMatrixEntry entry = allUIPairs.next();
                int item = entry.getItemIndex();
                int user = entry.getUserIndex();
                double ratingUI = entry.getValue();
                if (ratingUI <= 0) {
                    continue;
                }

                for (int k = 0; k < featureCount; k++) {
                    double gammaShpUK = gammaShp.getEntry(user, k);
                    double gammaRteUK = gammaRte.getEntry(user, k);
                    double lambdaShpIK = lambdaShp.getEntry(item, k);
                    double lambdaRteIK = lambdaRte.getEntry(item, k);
                    double phiUIK = Gamma.digamma(gammaShpUK) - Math.log(gammaRteUK) + Gamma.digamma(lambdaShpIK) - Math.log(lambdaRteIK);
                    phiUI.setEntry(k, phiUIK);
                }
                logNormalize(phiUI);

                if (ratingUI > 1) {
                    phiUI.mapMultiplyToSelf(ratingUI);
                }

                for (int k = 0; k < featureCount; k++) {
                    double value = phiUI.getEntry(k);
                    gammaShpNext.addToEntry(user, k, value);
                    lambdaShpNext.addToEntry(item, k, value);
                }


            }
            logger.info("iteration {} first phrase update finished", iterCount);

            RealVector gammaRteSecondTerm = MatrixUtils.createRealVector(new double[featureCount]);
            for (int k = 0; k < featureCount; k++) {
                double gammaRteUK = 0.0;
                for (int item = 0; item < itemNum; item++) {
                    gammaRteUK += lambdaShp.getEntry(item, k) / lambdaRte.getEntry(item, k);
                }
                gammaRteSecondTerm.setEntry(k, gammaRteUK);
            }

            // update user parameters
            double kappaRteFirstTerm = aPrime / bPrime;
            for (int user = 0; user < userNum; user++) {

                double gammaRteUKFirstTerm = kappaShp.getEntry(user) / kappaRte.getEntry(user);
                double kappaRteU = 0.0;

                for (int k = 0; k < featureCount; k++) {
                    double gammaShpUK = gammaShpNext.getEntry(user, k);
                    gammaShp.setEntry(user, k, gammaShpUK);
                    gammaShpNext.setEntry(user, k, a);
                    double gammaRteUK = gammaRteSecondTerm.getEntry(k);
                    gammaRteUK += gammaRteUKFirstTerm;
                    gammaRte.setEntry(user, k, gammaRteUK);
                    kappaRteU += gammaShpUK / gammaRteUK;
                }
                kappaRteU += kappaRteFirstTerm;
                kappaRte.setEntry(user, kappaRteU);
            }

            logger.info("iteration {} second phrase update finished", iterCount);


            RealVector lambdaRteSecondTerm = MatrixUtils.createRealVector(new double[featureCount]);
            for (int k = 0; k < featureCount; k++) {
                double lambdaRteIK = 0.0;
                for (int user = 0; user < userNum; user++) {
                    lambdaRteIK += gammaShp.getEntry(user, k) / gammaRte.getEntry(user, k);
                }
                lambdaRteSecondTerm.setEntry(k, lambdaRteIK);
            }

            // update item parameters
            double tauRteFirstTerm = cPrime / dPrime;
            for (int item = 0; item < itemNum; item++) {

                double lambdaRteFirstTerm = tauShp.getEntry(item) / tauRte.getEntry(item);
                double tauRteI = 0.0;

                for (int k = 0; k < featureCount; k++) {
                    double lambdaShpIK = lambdaShpNext.getEntry(item, k);
                    lambdaShp.setEntry(item, k, lambdaShpIK);
                    lambdaShpNext.setEntry(item, k, c);
                    double lambdaRteIK = lambdaRteSecondTerm.getEntry(k);

                    // plus first term
                    lambdaRteIK += lambdaRteFirstTerm;
                    lambdaRte.setEntry(item, k, lambdaRteIK);
                    // update tauRteI second term
                    tauRteI += lambdaShpIK / lambdaRteIK;
                }
                tauRteI += tauRteFirstTerm;
                tauRte.setEntry(item, tauRteI);
            }

            logger.info("iteration {} third phrase update finished", iterCount);

            // compute average predictive log likelihood of validation data per {@code iterationfrequency} iterations

            if (iterCount == 1) {
                for (int user = 0; user < userNum; user++) {
                    kappaShp.setEntry(user, kappaShpU);
                }
                for (int item = 0; item < itemNum; item++) {
                    tauShp.setEntry(item, tauShpI);
                }
            }

            if ((iterCount % iterationFrequency) == 0) {
                Iterator<RatingMatrixEntry> valIter = validation.iterator();
                avgPLLCurr = 0.0;

                while (valIter.hasNext()) {
                    RatingMatrixEntry ratingEntry = valIter.next();
                    int user = ratingEntry.getUserIndex();
                    int item = ratingEntry.getItemIndex();
                    double rating = ratingEntry.getValue();
                    double eThetaBeta = 0.0;
                    for (int k = 0; k < featureCount; k++) {
                        double eThetaUK = gammaShp.getEntry(user, k) / gammaRte.getEntry(user, k);
                        double eBetaIK = lambdaShp.getEntry(item, k) / lambdaRte.getEntry(item, k);
                        eThetaBeta += eThetaUK * eBetaIK;
                    }
                    double pLL = 0.0;
                    if (isProbPredition) {
                        pLL = (rating == 0) ? (-eThetaBeta) : Math.log(1 - Math.exp(-eThetaBeta));
                    } else {
                        pLL = rating * Math.log(eThetaBeta) - eThetaBeta - Gamma.logGamma(rating + 1);
                    }
                    avgPLLCurr += pLL;
                }
                avgPLLCurr = avgPLLCurr / validation.size();
                diffPLL = Math.abs((avgPLLCurr - avgPLLPre) / avgPLLPre);
                avgPLLPre = avgPLLCurr;
                logger.info("iteration {} with current average predictive log likelihood {} and the change is {}", iterCount, avgPLLCurr, diffPLL);
            }
            iterCount++;
        }

        // construct feature matrix used by HPFModel
        RealMatrix eTheta = MatrixUtils.createRealMatrix(userNum, featureCount);
        RealMatrix eBeta = MatrixUtils.createRealMatrix(itemNum, featureCount);
        for (int user = 0; user < userNum; user++) {
            RealVector gammaShpU = gammaShp.getRowVector(user);
            RealVector gammaRteU = gammaRte.getRowVector(user);
            RealVector eThetaU = gammaShpU.ebeDivide(gammaRteU);
            eTheta.setRowVector(user, eThetaU);
            logger.info("Training user {} features finished",user);
        }

        for (int item = 0; item < itemNum; item++) {
            RealVector lambdaShpI = lambdaShp.getRowVector(item);
            RealVector lambdaRteI = lambdaRte.getRowVector(item);
            RealVector eBetaI = lambdaShpI.ebeDivide(lambdaRteI);
            eBeta.setRowVector(item, eBetaI);
            logger.info("Training item {} features finished", item);
        }

        KeyIndex uidx = ratings.getUserIndex();
        KeyIndex iidx = ratings.getItemIndex();

        return new HPFModel(eTheta, eBeta, uidx, iidx);
    }

    /**
     * Initialization of parameter matrices
     * @param gammaShp
     * @param gammaRte
     * @param kappaRte
     * @param kappaShp
     * @param lambdaShp
     * @param lambdaRte
     * @param tauRte
     * @param tauShp
     */
    public void initialize(RealMatrix gammaShp, RealMatrix gammaRte, RealVector kappaRte, RealVector kappaShp,
                           RealMatrix lambdaShp, RealMatrix lambdaRte, RealVector tauRte, RealVector tauShp) {
        final int userNum = ratings.getUserIndex().size();
        final int itemNum = ratings.getItemIndex().size();
        final int featureCount = hyperParameters.getFeatureCount();
        final double a = hyperParameters.getUserWeightShpPrior();
        final double aPrime = hyperParameters.getUserActivityShpPrior();
        final double bPrime = hyperParameters.getUserActivityPriorMean();
        final double c = hyperParameters.getItemWeightShpPrior();
        final double cPrime = hyperParameters.getItemActivityShpPrior();
        final double dPrime = hyperParameters.getItemActivityPriorMean();
        // Initialization
        Random random = new Random(rndSeed);
        final double kRte = aPrime + featureCount;
        final double tRte = cPrime + featureCount;

        for (int u = 0; u < userNum; u++ ) {
            for (int k = 0; k < featureCount; k++) {
                double valueShp = a + maxOffsetShp*random.nextDouble();
                double valueRte = aPrime + maxOffsetRte*random.nextDouble();
                gammaShp.setEntry(u, k, valueShp);
                gammaRte.setEntry(u, k, valueRte);
            }

            double kShp = aPrime + maxOffsetShp*random.nextDouble();
            kappaRte.setEntry(u, kRte);
            kappaShp.setEntry(u, kShp);
        }

        for (int i = 0; i < itemNum; i++ ) {
            for (int k = 0; k < featureCount; k++) {
                double valueShp = c + maxOffsetShp*random.nextDouble();
                double valueRte = cPrime + maxOffsetRte*random.nextDouble();
                lambdaShp.setEntry(i, k, valueShp);
                lambdaRte.setEntry(i, k, valueRte);
            }
            double tShp = cPrime + maxOffsetShp*random.nextDouble();
            tauRte.setEntry(i, tRte);
            tauShp.setEntry(i, tShp);
        }

    }

    public void logNormalize (RealVector phi) {
        final int size = phi.getDimension();
        if (size == 1) {
            phi.setEntry(0, 1.0);
        }

        if (size > 1) {
            double logsum = phi.getEntry(0);
            for (int i = 1; i < size; i++) {
                double phiK = phi.getEntry(i);
                if (phiK < logsum) {
                    logsum = logsum + Math.log(1 + Math.exp(phiK - logsum));
                } else {
                    logsum = phiK + Math.log(1 + Math.exp(logsum - phiK));
                }
            }

            for (int k = 0; k < size; k++) {
                double phiK = phi.getEntry(k);
                double normalized = Math.exp(phiK - logsum);
                phi.setEntry(k, normalized);
            }
        }

    }
}
