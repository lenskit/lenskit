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
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Parallel Implementation of HPF recommender builder.
 *
 * <p>This recommender builder constructs an hierarchical Poisson matrix factorization recommender(HPF)
 * using a mean-field variational inference algorithm. These are documented in
 * <a href="https://arxiv.org/abs/1311.1704">Original paper: Scalable Recommendation with Poisson Factorization</a>.</p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HPFModelParallelProvider implements Provider<HPFModel> {
    private static Logger logger = LoggerFactory.getLogger(HPFModelParallelProvider.class);

    private final DataSplitStrategy ratings;
    private final PFHyperParameters hyperParameters;
    private final int iterationFrequency;
    private final double maxOffsetShp;
    private final double maxOffsetRte;
    private final long rndSeed;
    private final boolean isProbPrediction;
    private final double threshold;
    private final int maxIterCount;


    @Inject
    public HPFModelParallelProvider(@Transient DataSplitStrategy rndRatings,
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
        isProbPrediction = probPred;
        threshold = threshld;
        maxIterCount = maxIter;
    }

    @Override
    public HPFModel get() {

        final int userNum = ratings.getUserIndex().size();
        final int itemNum = ratings.getItemIndex().size();
        final int featureCount = hyperParameters.getFeatureCount();
        final double userWeightShpPrior = hyperParameters.getUserWeightShpPrior();
        final double userActivityShpPrior = hyperParameters.getUserActivityShpPrior();
        final double userActivityPriorMean = hyperParameters.getUserActivityPriorMean();
        final double itemWeightShpPrior = hyperParameters.getItemWeightShpPrior();
        final double itemActivityShpPrior = hyperParameters.getItemActivityShpPrior();
        final double itemActivityPriorMean = hyperParameters.getItemActivityPriorMean();

        PMFModel preUserModel = new PMFModel();
        PMFModel preItemModel = new PMFModel();
        Random random = new Random(rndSeed);
        preUserModel.initialize(userWeightShpPrior, userActivityShpPrior, userActivityPriorMean, userNum, featureCount, maxOffsetShp, maxOffsetRte, random);
        preItemModel.initialize(itemWeightShpPrior, itemActivityShpPrior, itemActivityPriorMean, itemNum, featureCount, maxOffsetShp, maxOffsetRte, random);
        logger.info("initialization finished");

        final List<RatingMatrixEntry> validation = ratings.getValidationRatings();

        Map<Integer, List<RatingMatrixEntry>> groupRatingsByUser = ratings.getTrainRatings()
                .parallelStream()
                .collect(groupingBy(RatingMatrixEntry::getUserIndex));
        Map<Integer, List<RatingMatrixEntry>> groupRatingsByItem = ratings.getTrainRatings()
                .parallelStream()
                .collect(groupingBy(RatingMatrixEntry::getItemIndex));

        //fill out dummy entry in order to allow parallel update to cover all indices
        for (int u = 0; u < userNum; u++) {
            if (!groupRatingsByUser.containsKey(u)) {
                List<RatingMatrixEntry> ratings = new ArrayList<>();

                RatingMatrixEntry entry = new DummyEntry(u);
                ratings.add(entry);
                groupRatingsByUser.put(u, ratings);
            }
        }

        for (int i = 0; i < itemNum; i++) {
            if (!groupRatingsByItem.containsKey(i)) {
                List<RatingMatrixEntry> ratings = new ArrayList<>();

                RatingMatrixEntry entry = new DummyEntry(i);
                ratings.add(entry);
                groupRatingsByItem.put(i, ratings);
            }
        }

        double avgPLLPre = Double.MAX_VALUE;
        double avgPLLCurr = 0.0;
        double diffPLL = 1.0;
        int iterCount = 1;

        while (iterCount < maxIterCount && diffPLL > threshold) {

            final PMFModel finalPreUserModel = preUserModel;
            final PMFModel finalPreItemModel = preItemModel;

            PMFModel currUserModel = groupRatingsByUser.values()
                    .parallelStream()
                    .map(e -> PMFModel.computeUserUpdate(e, finalPreUserModel, finalPreItemModel, hyperParameters))
                    .collect(new PMFModelCollector());
            logger.info("iteration {} user update finished", iterCount);

            PMFModel currItemModel = groupRatingsByItem.values()
                    .parallelStream()
                    .map(e -> PMFModel.computeItemUpdate(e, finalPreUserModel, finalPreItemModel, currUserModel, hyperParameters))
                    .collect(new PMFModelCollector());
            logger.info("iteration {} item update finished", iterCount);

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
                        double eThetaUK = currUserModel.getWeightShpEntry(user, k) / currUserModel.getWeightRteEntry(user, k);
                        double eBetaIK = currItemModel.getWeightShpEntry(item, k) / currItemModel.getWeightRteEntry(item, k);
                        eThetaBeta += eThetaUK * eBetaIK;
                    }
                    double pLL = 0.0;
                    if (isProbPrediction) {
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
            RealVector eThetaU = MatrixUtils.createRealVector(new double[featureCount]);
            for (int k = 0; k < featureCount; k++) {
                double gammaShpUK = preUserModel.getWeightShpEntry(user, k);
                double gammaRteUK = preUserModel.getWeightRteEntry(user, k);
                double value = gammaShpUK / gammaRteUK;
                eThetaU.setEntry(k, value);
            }

            eTheta.setRowVector(user, eThetaU);
            logger.info("Training user {} features finished",user);
        }

        for (int item = 0; item < itemNum; item++) {
            RealVector eBetaI = MatrixUtils.createRealVector(new double[featureCount]);
            for (int k = 0; k < featureCount; k++) {
                double lambdaShpIK = preItemModel.getWeightShpEntry(item, k);
                double lambdaRteIK = preItemModel.getWeightRteEntry(item, k);
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

    private class DummyEntry extends RatingMatrixEntry {
        final int index;
        public DummyEntry(int i) {
            index = i;
        }

        @Override
        public long getUserId() {
            return 0;
        }

        @Override
        public int getUserIndex() {
            return index;
        }

        @Override
        public long getItemId() {
            return 0;
        }

        @Override
        public int getItemIndex() {
            return index;
        }

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public double getValue() {
            return 0;
        }
    }

}
