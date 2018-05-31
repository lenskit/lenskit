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
package org.lenskit.predict.ordrec;

import it.unimi.dsi.fastutil.longs.*;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractRatingPredictor;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.results.AbstractResult;
import org.lenskit.results.Results;
import org.lenskit.transform.quantize.Quantizer;
import org.lenskit.util.math.Vectors;
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
    private DataAccessObject dao;
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
    public OrdRecRatingPredictor(ItemScorer scorer, DataAccessObject dao, Quantizer quantizer,
                                 @LearningRate double rate,
                                 @RegularizationTerm double reg,
                                 @IterationCount int niters) {
        this.dao = dao;
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
    OrdRecRatingPredictor(ItemScorer scorer, DataAccessObject dao, Quantizer q) {
        this.dao = dao;
        this.itemScorer = scorer;
        this.quantizer = q;
        this.learningRate = 1e-3;
        this.regTerm = 0.015;
        this.iterationCount = 1000;
    }

    /**
     * Extract a user vector from a data source.
     *
     * @param uid The user ID.
     * @param dao The DAO.
     * @return The user rating vector.
     */
    private Long2DoubleMap makeUserVector(long uid, DataAccessObject dao) {
        List<Rating> history = dao.query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, uid)
                                  .get();
        Long2DoubleMap vector = null;
        if (!history.isEmpty()) {
            vector = Ratings.userRatingVector(history);
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
    private void trainModel(OrdRecModel model, Long2DoubleMap ratings, Map<Long, Double> scores) {
        RealVector beta = model.getBeta();
        RealVector deltaBeta = new ArrayRealVector(beta.getDimension());
        double dt1;
        // n is the number of iteration;
        for (int j = 0; j < iterationCount; j++ ) {
            for (Long2DoubleMap.Entry rating: Vectors.fastEntries(ratings)) {
                long iid = rating.getLongKey();
                double score = scores.get(iid);
                int r = quantizer.index(rating.getDoubleValue());

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
        Long2DoubleMap ratings = makeUserVector(user, dao);
        if (ratings == null) {
            logger.warn("user {} has no ratings", user);
            ratings = Long2DoubleMaps.EMPTY_MAP;
        }
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

        OrdRecModel params = new OrdRecModel(quantizer);
        trainModel(params, ratings, scores);
        logger.debug("trained parameters for {}: {}", user, params);

        RealVector probabilities = new ArrayRealVector(params.getLevelCount());

        List<Result> results = new ArrayList<>();

        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            Double score = scores.get(item);
            if (score == null) {
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
