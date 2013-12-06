/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.predict.ordrec;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.basic.AbstractRatingPredictor;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.transform.quantize.Quantizer;
import org.grouplens.lenskit.vectors.*;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * The implementation for the ordrec algorithm.
 * The model views user feedback as ordinal. The framework is based on
 * a pointwise (rather than pairwise) ordinal approach, it can wrap existing
 * CF methods, and upgrade them into being able to tackle ordinal feedback.
 * The implementation is based on Koren's paper:
 * <a href="http://dl.acm.org/citation.cfm?doid=2043932.2043956">
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class OrdRecRatingPredictor extends AbstractRatingPredictor {
    private ItemScorer itemScorer;
    private UserEventDAO userEventDao;
    private Quantizer quantizer;
    private final double learningRate;
    private final double regTerm;
    private final int iterationCount;

    /**
     * This is a helper class contains all parameters the Ordrec need:
     * The thresholds t1 and beta. The rating value set and the number of
     * different rating values.
     *
     */
    private class OrdRecModel {

        private int levelCount;
        private double t1;
        private double[] beta;
        ImmutableVec qtzValues;

        /**
         * The constructor of OrdRecParameter.
         * It use the quantized values of rating to initialize t1 and beta.
         * Each threshold is initialized as the mean of two contiguous rating values.
         * Since the index of quantizer is always an successive non-negative integer
         * begin from 0, so t1 will initialize as 0.5, and the interval between two
         * thresholds will be 1.
         * @param qtz The quantizer for ratings
         */
        private OrdRecModel (Quantizer qtz) {
            qtzValues = qtz.getValues();
            levelCount = qtzValues.size();
            t1 = (qtzValues.get(0) + qtzValues.get(1))/2;
            beta = new double[levelCount-2];
            /*
//            I comment this part so that you can double check the correction of it
            for(int i = 1; i <= beta.length; i++ ) {
                beta[i-1] = Math.log((qtzValues.get(i+1)-qtzValues.get(i-1))/2);
            }
            */
            beta[0] = Math.log((qtzValues.get(1) + qtzValues.get(2)) / 2 - t1);
            for(int i = 2; i<= beta.length; i++) {
                beta[i-1] = Math.log((qtzValues.get(i) + qtzValues.get(i+1)) / 2 - beta[i-2]);
            }
        }

        /**
         * Get the first threshold t1
         *
         * @return the first threshold t1.
         */
        public double getT1() {
            return t1;
        }

        /**
         * Get beta set.
         *
         * @return beta set.
         */
        public double[] getBeta() {
            return beta;
        }

        /**
         * Get the count of rating levelCount.
         *
         * @return s The number of different ratings.
         */
        public int getLevelCount() {
            return levelCount;
        }

        /**
         * Get the rth threshold.
         *
         * @param thresholdIndex The index of the threshold
         * @return the rth threshold.
         */
        public double getThreshold(int thresholdIndex) {
            double tr = t1;
            if(thresholdIndex < 0) {
                return Double.NEGATIVE_INFINITY;
            } else if(thresholdIndex == 0){
                return tr;
            } else if(thresholdIndex > beta.length) {
                return Double.POSITIVE_INFINITY;
            } else {
                for(int k = 0; k < thresholdIndex; k++)
                    tr += Math.exp(beta[k]);
                return tr;
            }
        }

        /**
         * Get the probability of P(rui<=r|Theta)
         *
         * @param score The score of user uid and item iid.
         * @param r The index of rth threshold.
         * @return The probability
         */
        public double getProbLE(double score, int r) {

            return 1/(1 + Math.exp(score - getThreshold(r)));
        }

        /**
         * Get the probability of P(rui=r|Theta)
         *
         * @param score The score of user uid and item iid.
         * @param r The index of rth threshold.
         * @return The probability
         */
        public double getProbEQ(double score, int r) {
            return getProbLE(score, r) - getProbLE(score, r-1);
        }

        /**
         * This is a helper function to calculate derivative of parameters.
         * this function computes $\frac{d}{dx} (t_r - y_{ui})$, and that r specifies
         * what t_r is used, and k speficies x (with k=0, $x = t_1$; for k > 0, it is $x = β_k$).
         *
         * @param r The index of rth threshold
         * @param k The index of kth parameters need to derivative
         * @param beta The parameter need to derivative
         * @return The derivative of beta
         */
        private double derivateOfBeta(int r, int k, double beta) {
            if(r >= 0 && k == 0) {
                return 1.0;
            } else if (k > 0 && r >= k) {
                return Math.exp(beta);
            } else {
                return 0;
            }
        }


        /**
         * The train function of OrdRec. Get all parameters after learning process.
         */
        @SuppressWarnings("ConstantConditions")
        private void train(SparseVector ratings, MutableSparseVector scores) {


            double[] dbeta = new double[beta.length];
            double dt1;
            // n is the number of iteration;
            for (int j = 0; j < iterationCount; j++ ) {
                for(VectorEntry rating : ratings) {
                    long iid = rating.getKey();
                    double score = scores.get(iid);
                    int r = quantizer.index(rating.getValue());

                    //this is the first parameter and threshold, the gradient is different from any others:
                    dt1 = learningRate / getProbEQ(score,r) *
                            ( getProbLE(score,r) * (1 - getProbLE(score,r)) * derivateOfBeta(r, 0, t1) -
                                    getProbLE(score, r-1)*(1 - getProbLE(score, r-1)) * derivateOfBeta(r-1, 0, t1)
                                    - regTerm*t1);

                    for(int k = 0; k < beta.length; k++) {

                        dbeta[k] = learningRate / getProbEQ(score,r) *
                                ( getProbLE(score,r) * (1 - getProbLE(score,r)) * derivateOfBeta(r, k+1, beta[k]) -
                                        getProbLE(score, r-1)*(1 - getProbLE(score, r-1)) *
                                                derivateOfBeta(r-1, k+1, beta[k]) - regTerm*beta[k]);

                    }
                    t1 = t1 + dt1;
                    for(int k = 0; k < beta.length; k++) {
                        beta[k] = beta[k] + dbeta[k];
                    }
                }
            }
        }
    }

    /**
     * Constructor of OrdRecRatingPrediciton
     *
     * @param scorer The ItemScorer to create scores of each item
     * @param dao The UserEventDAO
     * @param quantizer The Quantizer used to quantizer the rating values
     * @param rate The LearningRate
     * @param reg The Regularization
     */
    @Inject
    public OrdRecRatingPredictor(ItemScorer scorer, UserEventDAO dao, Quantizer quantizer, @LearningRate double rate,
                                 @RegularizationTerm double reg, @IterationCount int niters) {
        this.userEventDao = dao;
        this.itemScorer = scorer;
        this.quantizer = quantizer;
        this.learningRate = rate;
        this.regTerm = reg;
        this.iterationCount = niters;

    }


    public OrdRecRatingPredictor(ItemScorer scorer, UserEventDAO dao, Quantizer q) {

        this.userEventDao = dao;
        this.itemScorer = scorer;
        this.quantizer = q;
        this.learningRate = 1e-3;
        this.regTerm = 0.015;
        this.iterationCount = 1000;
    }

    /**
     * It is used to generate rating list from UserEventDAO.
     *
     * @param uid The user ID.
     * @param dao The UserEventDAO.
     *
     * @return The VectorEntry list of rating.
     */
    private SparseVector makeUserVector(long uid, UserEventDAO dao) {
        UserHistory<Rating> history = dao.getEventsForUser(uid, Rating.class);
        SparseVector vector = null;
        if (history != null) {
            vector = RatingVectorUserHistorySummarizer.makeRatingVector(history);
        }

        return vector;
    }


    /**
     * Get the probability distribution according to score and thresholds
     * @param score The score
     * @param p The OrdRecParameters contains all parameter of OrdRec, used to get probability.
     * @return The double array of probability distribution.
     */
    public Vec getProbDistribution(double score, OrdRecModel p) {
        double[] distribution = new double[p.getLevelCount()];
        distribution[0] = p.getProbLE(score, 0);
        double pre = distribution[0];
        for(int i = 1; i < p.getLevelCount(); i++) {
            double pro = p.getProbLE(score, i);
            distribution[i] = pro - pre;
            pre = pro;
        }
        for(double d : distribution)
            System.out.print(d + ", ");
        System.out.println();
        return ImmutableVec.create(distribution);
    }


    @Override
    public void predict(long uid, @Nonnull MutableSparseVector predictions) {
        OrdRecModel para = new OrdRecModel(quantizer);
        SparseVector ratings = makeUserVector(uid, userEventDao);
        LongSet keySet = userEventDao.getEventsForUser(uid).itemSet();
        MutableSparseVector scores = MutableSparseVector.create(keySet);
        itemScorer.score(uid, scores);
        para.train(ratings, scores);

        for (VectorEntry e: predictions.fast(VectorEntry.State.EITHER)) {
            long iid = e.getKey();
            double score = itemScorer.score(uid, iid);
            Vec probabilities = getProbDistribution(score, para);
            int ratingIndex = probabilities.largestDimension();

            predictions.set(e, quantizer.getIndexValue(ratingIndex));
        }
    }
}
