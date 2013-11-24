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
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.transform.quantize.Quantizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * The implementation for the ordrec algorithm.
 * The model views user feedback as ordinal. The framework is based on
 * a pointwise (rather than pairwise) ordinal approach, it can wrap existing
 * CF methods, and upgrade them into being able to tackle ordinal feedback.
 * The implementation is based on Koren's paper:
 * <a href="http://dl.acm.org/citation.cfm?doid=2043932.2043956">
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class OrdRecRatingPredictor {
    private ItemScorer itemScorer;
    private UserEventDAO userEventDao;
    private Quantizer quantizer;
    private final double learningRate;
    private final double regTerm;

    /**
     * This is a helper class contains all parameters the Ordrec need:
     * The thresholds t1 and beta. The rating value set and the number of
     * different rating values.
     *
     */
    public class OrdRecParameter {
        private int s;
        private double t1;
        private double[] beta;
        private double[] quantizerValues;

        /**
         * The constructor of OrdRecParameter.
         * It use the quantized values of rating to initialize t1 and beta.
         * Each threshold is initialized as the mean of two contiguous rating values.
         * Since the index of quantizer is always an successive non-negative integer
         * begin from 0, so t1 will initialize as 0.5, and the interval between two
         * thresholds will be 1.
         * @param d The quantized rating values
         */
        public OrdRecParameter (double[] d) {

            quantizerValues = d;
            s = d.length;
            t1 = 0.5;
            beta = new double[s-2];
            for(int i = 0; i < beta.length; i++ ) {
                beta[i] = Math.log(1);
            }
        }


        /**
         * Get the rth threshold.
         *
         * @param r The index of the threshold
         * @return the rth threshold.
         */
        public double getThreshold(int r) {
            double tr = t1;
            if(r == 0){
                return tr;
            } else {
                if(r > s-2)
                    return quantizerValues[s-1];
                for(int k = 0; k < r; k++)
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
        public double getProbability(double score, int r) {
            if (r < 0)
                return 0;
            else if (r > s-2)
                return 1.0;
            else
                return 1/(1 + Math.exp(score - getThreshold(r)));
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
         * Set the first threshold t1
         *
         * @param t1 The first threshold
         */
        public void setT1(double t1) {
            this.t1 = t1;
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
         * set beta set.
         *
         * @param beta The double array
         */
        public void setBeta(double[] beta) {
            this.beta = beta;
        }

        /**
         * Get the count of rating S.
         *
         * @return s The number of different ratings.
         */
        public int getS() {
            return s;
        }

        /**
         * Set the S
         *
         * @param s The number of different ratings.
         */
        public void setS(int s) {
            this.s = s;
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
    public OrdRecRatingPredictor(ItemScorer scorer, UserEventDAO dao, Quantizer quantizer,
                                 @LearningRate double rate, @RegularizationTerm double reg) {
        this.userEventDao = dao;
        this.itemScorer = scorer;
        this.quantizer = quantizer;
        this.learningRate = rate;
        this.regTerm = reg;
//        this.stopCond = stop;

    }


    public OrdRecRatingPredictor(ItemScorer scorer, UserEventDAO dao, Quantizer q) {

        this.userEventDao = dao;
        this.itemScorer = scorer;
        this.quantizer = q;
        this.learningRate = 1e-3;
        this.regTerm = 0.015;
    }


    /**
     * This is a helper function to calculate derivate of parameters.
     *
     * @param r The index of rth threshold
     * @param k The index of kth parameters need to derivate
     * @param beta The parameter need to derivate
     * @return The derivate of beta
     */
    private double derivateOfBeta(int r, int k, double beta) {
        if(r >= 0 && k < 0) {
            return 1.0;
        } else if (k >= 0 && r > k) {
            return Math.exp(beta);
        } else {
            return 0;
        }
    }

    /**
     * It is used to generate rating list from UserEventDAO.
     *
     * @param uid The user ID.
     * @param dao The UserEventDAO.
     *
     * @return The VectorEntry list of rating.
     */
    private List<VectorEntry> makeUserVector(long uid, UserEventDAO dao) {
        List<VectorEntry> list = new ArrayList<VectorEntry>();

        UserHistory<Rating> history = dao.getEventsForUser(uid, Rating.class);
        SparseVector vector = null;
        if (history != null) {
            vector = RatingVectorUserHistorySummarizer.makeRatingVector(history);
        }
        if (vector != null) {
            for (VectorEntry v : vector) {
                list.add(v);
            }
        }
        return list;
    }

    /**
     * The update function of OrdRec. Get all parameters after learning process.
     *
     * @param uid The user ID
     * @param para The OrdRecParameter class contains all parameter to be used.
     */
    @SuppressWarnings("ConstantConditions")
    private void update(long uid, OrdRecParameter para) {
        double t1 = para.getT1();
        double[] beta = para.getBeta();
        int n = 450;
        List<VectorEntry> ratings = makeUserVector(uid, userEventDao);
        LongSet keyset = userEventDao.getEventsForUser(uid).itemSet();
        MutableSparseVector msv = MutableSparseVector.create(keyset);
        itemScorer.score(uid, msv);

        // n is the number of iteration;
        for (int j = 0; j < n; j++ ) {
            Iterator<VectorEntry> iter = ratings.iterator();
            while(iter.hasNext()) {
                VectorEntry rating = iter.next();
                long iid = rating.getKey();
                double score = msv.get(iid);
                int r = quantizer.index(rating.getValue());

                //this is the first parameter and threshold, the gradient is different from any others:
                double dt1 = learningRate / ( para.getProbability(score,r) - para.getProbability(score, r-1))  *
                        ( para.getProbability(score,r) * (1 - para.getProbability(score,r)) * derivateOfBeta(r, -1, t1) -
                                para.getProbability(score, r-1)*(1 - para.getProbability(score, r-1)) * derivateOfBeta(r-1, -1, t1)
                                - regTerm*t1);

                double[] dbeta = new double[beta.length];
                for(int k = 0; k < beta.length; k++) {

                    dbeta[k] = learningRate / ( para.getProbability(score,r) - para.getProbability(score, r-1))  *
                            ( para.getProbability(score,r) * (1 - para.getProbability(score,r)) * derivateOfBeta(r, k, beta[k]) -
                                    para.getProbability(score, r-1)*(1 - para.getProbability(score, r-1)) *
                                            derivateOfBeta(r-1, k, beta[k]) - regTerm*beta[k]);

                }
                t1 = t1 + dt1;
                for(int k = 0; k < beta.length; k++) {
                    beta[k] = beta[k] + dbeta[k];
                }
            }
        }

        para.setBeta(beta);
        para.setT1(t1);
    }

    /**
     * Get the probability distribution according to score and thresholds
     * @param score The score
     * @param p The OrdRecParameters contains all parameter of OrdRec, used to get probability.
     * @return The double array of probability distribution.
     */
    public double[] getProbabilityDistribution(double score, OrdRecParameter p) {
        double[] distribution = new double[p.getS()];
        distribution[0] = p.getProbability(score, 0);
        double pre = distribution[0];
        for(int i = 1; i < p.getS(); i++) {
            double pro = p.getProbability(score, i);
            distribution[i] = pro - pre;
            pre = pro;
        }
        return distribution;
    }

    /**
     * The prediction function. Use the user id and item id to get predicted rating
     * with the highest probability. it call update function to learn and get all parameters.
     *
     * @param uid The userID
     * @param iid The ItemID
     * @return The predicated rating.
     */
    public double prediction(long uid, long iid) {
        double d[] = quantizer.getValues();
        OrdRecParameter para = new OrdRecParameter(d);
        update(uid, para);
        double score = itemScorer.score(uid, iid);
        double[] probabilities = getProbabilityDistribution(score, para);
        double highestProbability = probabilities[0];
        int ratingIndex = 0;

        for(int r = 1; r < probabilities.length; r++) {
            if(probabilities[r] > highestProbability) {
                highestProbability = probabilities[r];
                ratingIndex = r;
            }
        }
        return quantizer.getIndexValue(ratingIndex);
    }
}
