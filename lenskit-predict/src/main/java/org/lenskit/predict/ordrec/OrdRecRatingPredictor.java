/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.predict.ordrec;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.lenskit.data.ratings.Rating;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractRatingPredictor;
import org.lenskit.results.AbstractResult;
import org.lenskit.results.Results;
import org.lenskit.transform.quantize.Quantizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OrdRec implementation of rating prediction.
 *
 * The model views user feedback as ordinal. The framework is based on
 * a pointwise (rather than pairwise) ordinal approach, it can wrap existing
 * CF methods, and upgrade them into being able to tackle ordinal feedback.
 * The implementation is based on <a href="http://dl.acm.org/citation.cfm?doid=2043932.2043956">Koren's paper</a>:
 *
 * @since 2.1
 */
public class OrdRecRatingPredictor extends AbstractRatingPredictor {
    private static final Logger logger = LoggerFactory.getLogger(OrdRecRatingPredictor.class);

    private ItemScorer itemScorer;
    private UserEventDAO userEventDao;
    private Quantizer quantizer;
    private final double learningRate;
    private final double regTerm;
    private final int iterationCount;

    /**
     * Construct a new OrdRec rating predictor.
     *
     * @param scorer The ItemScorer to produce the underlyign scores.
     * @param dao The DAO to access user events.
     * @param quantizer The quantizer to which ratings should be constrained.
     * @param rate The learning rate for user profile training.
     * @param reg Regularization term for user profile training.
     */
    @Inject
    public OrdRecRatingPredictor(ItemScorer scorer, UserEventDAO dao, Quantizer quantizer,
                                 @LearningRate double rate,
                                 @RegularizationTerm double reg,
                                 @IterationCount int niters) {
        this.userEventDao = dao;
        this.itemScorer = scorer;
        this.quantizer = quantizer;
        this.learningRate = rate;
        this.regTerm = reg;
        this.iterationCount = niters;
    }

    /**
     * Convenience constructor for testing.
     * @param scorer The item scorer.
     * @param dao The user event DAO.
     * @param q The quantizer.
     */
    OrdRecRatingPredictor(ItemScorer scorer, UserEventDAO dao, Quantizer q) {
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
     * This is a helper function to calculate derivative of parameters.
     * this function computes $\frac{d}{dx} (t_r - y_{ui})$, and that r specifies
     * what t_r is used, and k speficies x (with k=0, $x = t_1$; for k &gt; 0, it is $x = β_k$).
     *
     * @param r The index of rth threshold
     * @param k The index of kth parameters need to derivative
     * @param beta The parameter need to derivative
     * @return The derivative of beta
     */
    private static double dBeta(int r, int k, double beta) {
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
    private void trainModel(OrdRecModel model, SparseVector ratings, MutableSparseVector scores) {
        RealVector beta = model.getBeta();
        RealVector deltaBeta = new ArrayRealVector(beta.getDimension());
        double dt1;
        // n is the number of iteration;
        for (int j = 0; j < iterationCount; j++ ) {
            for (VectorEntry rating: ratings) {
                long iid = rating.getKey();
                double score = scores.get(iid);
                int r = quantizer.index(rating.getValue());

                double probEqualR = model.getProbEQ(score, r);
                double probLessR = model.getProbLE(score, r);
                double probLessR_1 = model.getProbLE(score, r - 1);

                double t1 = model.getT1();
                dt1 = learningRate / probEqualR * ( probLessR * (1 - probLessR) * dBeta(r, 0, t1)
                        - probLessR_1 * (1 - probLessR_1) * dBeta(r - 1, 0, t1) - regTerm*t1);

                double dbetaK;
                for(int k = 0; k < beta.getDimension(); k++) {
                    dbetaK = learningRate / probEqualR * ( probLessR * (1 - probLessR) *
                            dBeta(r, k + 1, beta.getEntry(k)) - probLessR_1 * (1 - probLessR_1) *
                            dBeta(r - 1, k + 1, beta.getEntry(k)) - regTerm*beta.getEntry(k));
                    deltaBeta.setEntry(k, dbetaK);
                }
                model.update(dt1, deltaBeta);
            }
        }
    }

    @Nonnull
    @Override
    public Map<Long, Double> predict(long user, @Nonnull Collection<Long> items) {
        return computePredictions(user, items, false).scoreMap();
    }

    /**
     * Compute detailed predictions for the user.
     * @param user  The user ID for whom to generate predictions.
     * @param items The items to predict for.
     * @return The detailed results; each result is an instance of {@link OrdRecRatingPredictor.FullResult}.
     */
    @Nonnull
    @Override
    public ResultMap predictWithDetails(long user, @Nonnull Collection<Long> items) {
        return computePredictions(user, items, true);
    }

    @Nonnull
    private ResultMap computePredictions(long user, @Nonnull Collection<Long> items, boolean includeDetails) {
        logger.debug("predicting {} items for {}", items.size(), user);
        SparseVector ratings = makeUserVector(user, userEventDao);
        LongSet allItems = new LongOpenHashSet(ratings.keySet());
        allItems.addAll(items);

        ResultMap baseResults = null;
        Map<Long,Double> scores;
        if (includeDetails) {
            baseResults = itemScorer.scoreWithDetails(user, allItems);
            scores = baseResults.scoreMap();
        } else {
            scores = itemScorer.score(user, allItems);
        }
        MutableSparseVector scoreVector = MutableSparseVector.create(scores);

        OrdRecModel params = new OrdRecModel(quantizer);
        trainModel(params, ratings, scoreVector);
        logger.debug("trained parameters for {}: {}", user, params);

        RealVector probabilities = new ArrayRealVector(params.getLevelCount());

        List<Result> results = new ArrayList<>();

        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            double score = scoreVector.get(item, Double.NaN);
            if (Double.isNaN(score)) {
                continue;
            }
            params.getProbDistribution(score, probabilities);
            int mlIdx = probabilities.getMaxIndex();
            double pred = quantizer.getIndexValue(mlIdx);
            if (includeDetails) {
                results.add(new FullResult(baseResults.get(item), pred,
                                           new ArrayRealVector(probabilities)));
            } else {
                results.add(Results.create(item, pred));
            }
        }

        return Results.newResultMap(results);
    }

    /**
     * The result type of OrdRec rating predictions.
     */
    public static class FullResult extends AbstractResult implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Result original;
        private final RealVector distribution;

        /**
         * Create a new full result.
         * @param orig The original score.
         * @param score The estimated score.
         * @param probs The full probability distribution (defensive copy will not be taken).
         */
        FullResult(Result orig, double score, RealVector probs) {
            super(orig.getId(), score);
            original = orig;
            distribution = probs;
        }

        /**
         * Get the original result.
         * @return The original result.
         */
        public Result getOriginalResult() {
            return original;
        }

        /**
         * Get the probability distribution from this result.
         * @return The probability distribution.  It is **not** copied, so this vector should not be modified.
         */
        public RealVector getDistribution() {
            return distribution;
        }
    }
}
