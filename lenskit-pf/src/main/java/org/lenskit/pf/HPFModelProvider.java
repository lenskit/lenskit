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
        final Int2ObjectMap<ImmutableSet<Integer>> userItems = ratings.getUserItemIndices();
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

        // Initialization
        final Random random = new Random(rndSeed);
        double kShp = aPrime + featureCount * a;
        double tShp = cPrime + featureCount * c;

        for (int u = 0; u < userNum; u++ ) {
            for (int k = 0; k < featureCount; k++) {
                double valueShp = a + maxOffsetShp*random.nextDouble();
                double valueRte = bPrime + maxOffsetRte*random.nextDouble(); //not sure
                gammaShp.setEntry(u, k, valueShp);
                gammaRte.setEntry(u, k, valueRte);
//                valueRte = gammaRte.getEntry(0, k);// make rate parameter have
//                gammaRte.setEntry(u, k, valueRte); // same initials cross user delete these two line
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
//                valueRte = lambdaRte.getEntry(0, k); // make rate parameter have
//                lambdaRte.setEntry(i, k, valueRte); // same initials cross user delete these two line
            }
            double value = dPrime + maxOffsetRte*random.nextDouble();
            tauRte.setEntry(i, value);
            tauShp.setEntry(i, tShp);
        }

        final Int2ObjectMap<Int2DoubleMap> train = ratings.getTrainingMatrix();
        final List<RatingMatrixEntry> validation = ratings.getValidationRatings();
        TrainingLoopController controller = stoppingCondition.newLoop();
        double avgPLLPre = Double.MAX_VALUE;
        double avgPLLCurr = 0.0;
        double diffPLL = 1.0;

        while (controller.keepTraining(diffPLL)) {
            Int2ObjectMap<Int2ObjectMap<RealVector>> phi = new Int2ObjectOpenHashMap<>(userNum);

            // update phi
            Iterator<Map.Entry<Integer,Int2DoubleMap>> itemIter = train.entrySet().iterator();
            while (itemIter.hasNext()) {
                Map.Entry<Integer,Int2DoubleMap> itemEntry = itemIter.next();
                int item = itemEntry.getKey();
                Int2DoubleMap itemRaings = itemEntry.getValue();
                IntIterator userIter = itemRaings.keySet().iterator();
                while (userIter.hasNext()) {
                    int user = userIter.nextInt();
                    Int2ObjectMap<RealVector> phiUIsVec = phi.get(user);
                    if (phiUIsVec == null) phiUIsVec = new Int2ObjectOpenHashMap<>(itemNum);
                    RealVector phiUI = phiUIsVec.get(item);
                    if (phiUI == null) phiUI = MatrixUtils.createRealVector(new double[featureCount]);

                    for (int k = 0; k < featureCount; k++) {
                        double gammaShpUK = gammaShp.getEntry(user, k);
                        double gammaRteUK = gammaRte.getEntry(user, k);
                        double lambdaShpIK = lambdaShp.getEntry(item, k);
                        double lambdaRteIK = lambdaRte.getEntry(item, k);
                        double power = Gamma.digamma(gammaShpUK) - Math.log(gammaRteUK) + Gamma.digamma(lambdaShpIK) - Math.log(lambdaRteIK);
                        double phiUIK = Math.exp(power);
                        phiUI.setEntry(k, phiUIK);
                    }
                    double sumOfElements = phiUI.getL1Norm();
                    phiUI.mapDivideToSelf(sumOfElements);
                    phiUIsVec.put(item,phiUI);
                    phi.put(user, phiUIsVec);
                }
            }

            // update user parameters
            USER_OUTER:for (int user=0; user < userNum; user++) {
                Set<Integer> items = userItems.get(user);
//                if (items == null) items = Collections.emptySet();
                if (items == null) continue USER_OUTER;
                double kappaRteU = 0.0;

                for (int k = 0; k < featureCount; k++) {
                    double gammaShpUK = 0.0;
                    double gammaRteUK = 0.0;
                    for (int item : items) {
                        double yUI = train.get(item).get(user);
                        double phiUIK = phi.get(user).get(item).getEntry(k);
                        //plus second term
                        gammaShpUK += yUI * phiUIK;
                        gammaRteUK += lambdaShp.getEntry(item, k) / lambdaRte.getEntry(item, k);
                    }
                    // plus first term
                    gammaShpUK += a;
                    gammaRteUK += kappaShp.getEntry(user) / kappaRte.getEntry(user);
                    gammaShp.setEntry(user, k, gammaShpUK);
                    gammaRte.setEntry(user, k, gammaRteUK);
                    // update kappaRteU second term
                    kappaRteU += gammaShpUK / gammaRteUK;
                }
                kappaRteU += aPrime / bPrime;
                kappaRte.setEntry(user, kappaRteU);
            }

            // update item parameters
            ITEM_OUTER:for (int item = 0; item < itemNum; item++) {

                Int2DoubleMap itemRatings = train.get(item);
//                if (itemRatings == null) itemRatings = Int2DoubleMaps.EMPTY_MAP;
                if (itemRatings == null) continue ITEM_OUTER;
                double tauRteI = 0.0;

                for (int k = 0; k < featureCount; k++) {
                    double lambdaShpIK = 0.0;
                    double lambdaRteIK = 0.0;
                    Iterator<Map.Entry<Integer,Double>> itemRatingsIter = itemRatings.entrySet().iterator();
                    while (itemRatingsIter.hasNext()) {
                        Map.Entry<Integer,Double> itemRatingEntry = itemRatingsIter.next();
                        int user = itemRatingEntry.getKey();
                        double yUI = itemRatingEntry.getValue();
                        double phiUIK = phi.get(user).get(item).getEntry(k);
                        //plus second term
                        lambdaShpIK += yUI * phiUIK;
                        lambdaRteIK += gammaShp.getEntry(user, k) / gammaRte.getEntry(user, k);
                    }
                    // plus first term
                    lambdaShpIK += c;
                    lambdaRteIK += tauShp.getEntry(item) / tauRte.getEntry(item);
                    lambdaShp.setEntry(item, k, lambdaShpIK);
                    lambdaRte.setEntry(item, k, lambdaRteIK);
                    // update tauRteI second term
                    tauRteI += lambdaShpIK / lambdaRteIK;
                }
                tauRteI += cPrime / dPrime;
                tauRte.setEntry(item, tauRteI);
            }

            // compute average predictive log likelihood of validation data per {@code iterationfrequency} iterations
            int iterCount = controller.getIterationCount();
            if ((iterCount % iterationFrequency) == 0) {
                Iterator<RatingMatrixEntry> valIter = validation.iterator();

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
                System.out.println("iteration {" + iterCount + "} with average predictive log likelihood {" + avgPLLCurr + "} and the change is {" + diffPLL + "}");
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
}
