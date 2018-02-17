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
package org.lenskit.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.iterative.IterationCount;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.RatingMatrix;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.inject.Shareable;
import org.lenskit.inject.Transient;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Baseline scorer using least-squares estimates of preferences, trained by gradient descent.
 */
@DefaultProvider(LeastSquaresItemScorer.Builder.class)
@Shareable
public class LeastSquaresItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2DoubleSortedMap userBiases;
    private final Long2DoubleSortedMap itemBiases;
    private final double globalMean;

    private static final Logger logger = LoggerFactory.getLogger(LeastSquaresItemScorer.class);

    /**
     * Construct a new least-squares scorer.
     *
     * @param ubs the user biases
     * @param ibs the item biases
     * @param mean the global mean rating
     */
    public LeastSquaresItemScorer(Long2DoubleMap ubs, Long2DoubleMap ibs, double mean) {
        this.userBiases = LongUtils.frozenMap(ubs);
        this.itemBiases = LongUtils.frozenMap(ibs);
        this.globalMean = mean;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        double userScore = globalMean + userBiases.get(user);

        List<Result> results = new ArrayList<>();
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            double score = userScore + itemBiases.get(item);
            results.add(Results.create(item, score));
        }
        return Results.newResultMap(results);
    }

    /**
     * The builder for the least squares predictor.
     */
    public static class Builder implements Provider<LeastSquaresItemScorer> {
        private final double learningRate;
        private final double regularizationFactor;
        private final int maxIterations;
        private RatingMatrix snapshot;

        /**
         * Create a new builder.
         *
         * @param regFactor The regularization term
         * @param lrate     The learning rate
         * @param data      The preference data
         * @param maxIters  The maximum iteration count
         */
        @Inject
        public Builder(@RegularizationTerm double regFactor, @LearningRate double lrate,
                       @Transient RatingMatrix data,
                       @IterationCount int maxIters) {
            regularizationFactor = regFactor;
            learningRate = lrate;
            snapshot = data;
            maxIterations = maxIters;
        }

        @Override
        public LeastSquaresItemScorer get() {
            Collection<RatingMatrixEntry> ratings = snapshot.getRatings();
            logger.debug("training predictor on {} ratings", ratings.size());

            double sum = 0.0;
            double n = 0;
            for (RatingMatrixEntry r : ratings) {
                sum += r.getValue();
                n += 1;
            }
            final double mean = n > 0 ? sum / n : 0;
            logger.debug("mean rating is {}", mean);

            // TODO Use vectorz vectors instead of raw arrays
            double uoff[] = new double[snapshot.getUserIds().size()];
            double ioff[] = new double[snapshot.getItemIds().size()];

            double rmse = 0.0;
            for (int i = 0; i < maxIterations; i++) {
                double sse = 0;
                for (RatingMatrixEntry r : ratings) {
                    final int uidx = r.getUserIndex();
                    final int iidx = r.getItemIndex();
                    final double p = mean + uoff[uidx] + ioff[iidx];
                    final double err = r.getValue() - p;
                    uoff[uidx] += learningRate * (err - regularizationFactor * Math.abs(uoff[uidx]));
                    ioff[iidx] += learningRate * (err - regularizationFactor * Math.abs(ioff[iidx]));
                    sse += err * err;
                }
                rmse = Math.sqrt(sse / ratings.size());

                logger.debug("finished iteration {} (RMSE={})", i, rmse);
            }

            logger.info("trained baseline on {} ratings in {} iterations (final rmse={})", ratings.size(), maxIterations, rmse);

            // Convert the uoff array to a SparseVector

            Long2DoubleMap svuoff = Long2DoubleSortedArrayMap.fromArray(snapshot.userIndex(), uoff);
            // Convert the ioff array to a SparseVector
            Long2DoubleMap svioff = Long2DoubleSortedArrayMap.fromArray(snapshot.itemIndex(), ioff);
            return new LeastSquaresItemScorer(svuoff, svioff, mean);
        }
    }
}
