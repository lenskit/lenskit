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
import org.apache.commons.math3.special.Gamma;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.KeyIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.stream.Collectors.groupingBy;

/**
 * HPF recommender builder based on the paper "Scalable Recommendation with Poisson Factorization".
 *
 * <p>
 * This recommender builder constructs an hierarchical Poisson matrix factorization recommender(HPF)
 * using a mean-field variational inference algorithm.
 * These are documented in
 * <a href="https://arxiv.org/abs/1311.1704">Original paper: Scalable Recommendation with Poisson Factorization</a>.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HPFModelParallelProvider implements Provider<HPFModel> {
    private static Logger logger = LoggerFactory.getLogger(HPFModelParallelProvider.class);

    private final DataSplitStrategy ratings;
    private final StoppingCondition stoppingCondition;
    private final PFHyperParameters hyperParameters;
    private final int iterationFrequency;
    private final double maxOffsetShp;
    private final double maxOffsetRte;
    private final long rndSeed;
    private final boolean isProbPredition;


    @Inject
    public HPFModelParallelProvider(@Transient DataSplitStrategy rndRatings,
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

        PMFModel preUserModel = new PMFModel();
        PMFModel preItemModel = new PMFModel();
        Random random = new Random(rndSeed);
        preUserModel.initialize(a, aPrime, bPrime, userNum, featureCount, maxOffsetShp, maxOffsetRte, random);
        preItemModel.initialize(c, cPrime, dPrime, itemNum, featureCount, maxOffsetShp, maxOffsetRte, random);
        logger.info("initialization finished");

        final List<RatingMatrixEntry> validation = ratings.getValidationRatings();

        final Map<Integer, List<RatingMatrixEntry>> groupRatingsByUser = ratings.getTrainingMatrix().parallelStream().collect(groupingBy(RatingMatrixEntry::getUserIndex));
        final Map<Integer, List<RatingMatrixEntry>> groupRatingsByItem = ratings.getTrainingMatrix().parallelStream().collect(groupingBy(RatingMatrixEntry::getItemIndex));


        TrainingLoopController controller = stoppingCondition.newLoop();
        double avgPLLPre = Double.MAX_VALUE;
        double avgPLLCurr = 0.0;
        double diffPLL = 1.0;

        while (controller.keepTraining(diffPLL)) {

            int iterCount = controller.getIterationCount();

            PMFModel finalPreUserModel = preUserModel;
            PMFModel finalPreItemModel = preItemModel;

            PMFModel currUserModel = groupRatingsByUser.values().parallelStream().map(e -> PMFModel.computeUserUpdate(e, finalPreUserModel, finalPreItemModel, hyperParameters)).collect(new PMFModelCollector());

            PMFModel currItemModel = groupRatingsByItem.values().parallelStream().map(e -> PMFModel.computeItemUpdate(e, finalPreUserModel, finalPreItemModel, currUserModel, hyperParameters)).collect(new PMFModelCollector());

            preUserModel = currUserModel;
            preItemModel = currItemModel;

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
                        double eThetaUK = currUserModel.getGammaOrLambdaShpEntry(user, k) / currUserModel.getGammaOrLambdaRteEntry(user, k);
                        double eBetaIK = currItemModel.getGammaOrLambdaShpEntry(item, k) / currItemModel.getGammaOrLambdaRteEntry(item, k);
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
            RealVector eThetaU = MatrixUtils.createRealVector(new double[featureCount]);
            for (int k = 0; k < featureCount; k++) {
                double gammaShpUK = preUserModel.getGammaOrLambdaShpEntry(user, k);
                double gammaRteUK = preUserModel.getGammaOrLambdaRteEntry(user, k);
                double value = gammaShpUK / gammaRteUK;
                eThetaU.setEntry(k, value);
            }

            eTheta.setRowVector(user, eThetaU);
            logger.info("Training user {} features finished",user);
        }

        for (int item = 0; item < itemNum; item++) {
            RealVector eBetaI = MatrixUtils.createRealVector(new double[featureCount]);
            for (int k = 0; k < featureCount; k++) {
                double lambdaShpIK = preItemModel.getGammaOrLambdaShpEntry(item, k);
                double lambdaRteIK = preItemModel.getGammaOrLambdaRteEntry(item, k);
                double value = lambdaShpIK / lambdaRteIK;
                eBetaI.setEntry(k, value);
            }
            eBeta.setRowVector(item, eBetaI);
            logger.info("Training item {} features finished", item);
        }

        KeyIndex uidx = ratings.getUserIndex();
        KeyIndex iidx = ratings.getItemIndex();

        return new HPFModel(eTheta, eBeta, uidx, iidx);
    }

}
