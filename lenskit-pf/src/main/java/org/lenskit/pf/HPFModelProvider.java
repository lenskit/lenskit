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

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.special.Gamma;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.KeyIndex;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

/**
 * HPF recommender builder based on the paper "Scalable Recommendation with Poisson Factorization".
 *
 * <p>
 * This recommender builder constructs an hierarchical Poisson matrix factorization recommender(HPF)
 * using a mean-field variational inference algorithm.
 * These are documented in
 * <a href="https://arxiv.org/abs/1311.1704">Original paper: Scalable Recommendation with Poisson Factorization</a>.
 * This implementation is based in part on
 * <a href="https://github.com/premgopalan/hgaprec">The authors' github repository</a>.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HPFModelProvider implements Provider<HPFModel> {
    private static Logger logger = LoggerFactory.getLogger(HPFModelProvider.class);

    private final DataSplitStrategy ratings;
    private final StoppingCondition stoppingCondition;
    private final PFHyperParameters hyperParameters;
    private final int iterationFrequency;
    private final double maxOffsetShp;
    private final double maxOffsetRte;
    private final long rndSeed;
    private final boolean isProbPredition;


    @Inject
    public HPFModelProvider(@Transient DataSplitStrategy rndRatings,
                            StoppingCondition stop,
                            PFHyperParameters hyperParams,
                            @IterationFrequency int iterFreq,
                            @RandomSeed int seed,
                            @MaxRandomOffsetForShape double maxOffS,
                            @MaxRandomOffsetForRate double maxOffR,
                            @IsProbabilityPrediciton boolean probPred) {

        ratings = rndRatings;
        stoppingCondition = stop;
        hyperParameters = hyperParams;
        iterationFrequency = iterFreq;
        rndSeed = seed;
        maxOffsetShp = maxOffS;
        maxOffsetRte = maxOffR;
        isProbPredition = probPred;
    }

    @Override
    public HPFModel get() {

        final int userNum = ratings.getUserIndex().size();
        final int itemNum = ratings.getItemIndex().size();
        final int featureCount = hyperParameters.getFeatureCount();
        final double a = hyperParameters.getA();
        final double aPrime = hyperParameters.getAPrime();
        final double bPrime = hyperParameters.getBPrime();
        final double c = hyperParameters.getC();
        final double cPrime = hyperParameters.getCPrime();
        final double dPrime = hyperParameters.getDPrime();
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

        final Int2ObjectMap<Int2DoubleMap> train = ratings.getTrainingMatrix();
        final List<RatingMatrixEntry> validation = ratings.getValidationRatings();
        TrainingLoopController controller = stoppingCondition.newLoop();
        double avgPLLPre = Double.MAX_VALUE;
        double avgPLLCurr = 0.0;
        double diffPLL = 1.0;

        while (controller.keepTraining(diffPLL)) {

            // update phi
            ObjectIterator<Int2ObjectMap.Entry<Int2DoubleMap>> itemIter = train.int2ObjectEntrySet().iterator();
            while (itemIter.hasNext()) {
                Int2ObjectMap.Entry<Int2DoubleMap> itemEntry = itemIter.next();
                int item = itemEntry.getIntKey();
                Int2DoubleMap itemRaings = itemEntry.getValue();
                ObjectIterator<Int2DoubleMap.Entry> userIter = itemRaings.int2DoubleEntrySet().iterator();
                while (userIter.hasNext()) {
                    Int2DoubleMap.Entry userEntry = userIter.next();
                    int user = userEntry.getIntKey();
                    double ratingUI = userEntry.getDoubleValue();

                    for (int k = 0; k < featureCount; k++) {
                        double gammaShpUK = gammaShp.getEntry(user, k);
                        double gammaRteUK = gammaRte.getEntry(user, k);
                        double lambdaShpIK = lambdaShp.getEntry(item, k);
                        double lambdaRteIK = lambdaRte.getEntry(item, k);
                        double phiUIK = Gamma.digamma(gammaShpUK) - Math.log(gammaRteUK) + Gamma.digamma(lambdaShpIK) - Math.log(lambdaRteIK);
                        phiUI.setEntry(k, phiUIK);
                    }
                    logNormalize(phiUI);
                    double sumOfPhi = phiUI.getL1Norm();
//                    logger.info("Sum of phi vector is {}", sumOfPhi);


                    if (ratingUI > 1) {
                        phiUI.mapMultiplyToSelf(ratingUI);
                    }

                    for (int k = 0; k < featureCount; k++) {
                        double value = phiUI.getEntry(k);
                        gammaShpNext.addToEntry(user, k, value);
                        lambdaShpNext.addToEntry(item, k, value);
                    }

                }
            }

            // update user parameters
            for (int user = 0; user < userNum; user++) {

                double gammaRteUKFirstTerm = kappaShp.getEntry(user) / kappaRte.getEntry(user);
                double kappaRteU = 0.0;

                for (int k = 0; k < featureCount; k++) {
                    double gammaShpUK = gammaShpNext.getEntry(user, k);
                    gammaShp.setEntry(user, k, gammaShpUK);
                    gammaShpNext.setEntry(user, k, a);
                    double gammaRteUK = 0.0;
                    for (int item = 0; item < itemNum; item++) {
                        gammaRteUK += lambdaShp.getEntry(item, k) / lambdaRte.getEntry(item, k);
                    }
                    gammaRteUK += gammaRteUKFirstTerm;
                    gammaRte.setEntry(user, k, gammaRteUK);
                    kappaRteU += gammaShpUK / gammaRteUK;
                }
                kappaRteU += aPrime / bPrime;
                kappaRte.setEntry(user, kappaRteU);
            }

            // update item parameters
            for (int item = 0; item < itemNum; item++) {

                double lambdaRteFirstTerm = tauShp.getEntry(item) / tauRte.getEntry(item);
                double tauRteI = 0.0;

                for (int k = 0; k < featureCount; k++) {
                    double lambdaShpIK = lambdaShpNext.getEntry(item, k);
                    lambdaShp.setEntry(item, k, lambdaShpIK);
                    lambdaShpNext.setEntry(item, k, c);
                    double lambdaRteIK = 0.0;

                    for (int user = 0; user < userNum; user++) {
                        lambdaRteIK += gammaShp.getEntry(user, k) / gammaRte.getEntry(user, k);
                    }
                    // plus first term
                    lambdaRteIK += lambdaRteFirstTerm;
                    lambdaRte.setEntry(item, k, lambdaRteIK);
                    // update tauRteI second term
                    tauRteI += lambdaShpIK / lambdaRteIK;
                }
                tauRteI += cPrime / dPrime;
                tauRte.setEntry(item, tauRteI);
            }


            // compute average predictive log likelihood of validation data per {@code iterationfrequency} iterations
            int iterCount = controller.getIterationCount();
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
//                System.out.println("iteration {" + iterCount + "} with average predictive log likelihood {" + avgPLLCurr + "} and the change is {" + diffPLL + "}");
            }
//            System.out.println("iteration {" + iterCount + "} with average predictive log likelihood {" + avgPLLCurr + "}");

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
        final double a = hyperParameters.getA();
        final double aPrime = hyperParameters.getAPrime();
        final double bPrime = hyperParameters.getBPrime();
        final double c = hyperParameters.getC();
        final double cPrime = hyperParameters.getCPrime();
        final double dPrime = hyperParameters.getDPrime();
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
//                valueRte = gammaRte.getEntry(0, k);// make rate parameter have
//                gammaRte.setEntry(u, k, valueRte); // same initials cross user delete these two line
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
//                valueRte = lambdaRte.getEntry(0, k); // make rate parameter have
//                lambdaRte.setEntry(i, k, valueRte); // same initials cross user delete these two line
            }
            double tShp = cPrime + maxOffsetShp*random.nextDouble();
            tauRte.setEntry(i, tRte);
            tauShp.setEntry(i, tShp);
        }
        logger.info("initialization finished");
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
