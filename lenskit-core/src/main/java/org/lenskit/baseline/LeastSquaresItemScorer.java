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
package org.lenskit.baseline;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
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
        private PreferenceSnapshot snapshot;
        private StoppingCondition stoppingCondition;

        /**
         * Create a new builder.
         *
         * @param regFactor The regularization term
         * @param lrate     The learning rate
         * @param data      The preference data
         * @param stop      The training loop condition.
         */
        @Inject
        public Builder(@RegularizationTerm double regFactor, @LearningRate double lrate,
                       @Transient PreferenceSnapshot data,
                       StoppingCondition stop) {
            regularizationFactor = regFactor;
            learningRate = lrate;
            snapshot = data;
            stoppingCondition = stop;
        }

        @Override
        public LeastSquaresItemScorer get() {
            Collection<IndexedPreference> ratings = snapshot.getRatings();
            logger.debug("training predictor on {} ratings", ratings.size());

            double sum = 0.0;
            double n = 0;
            for (IndexedPreference r : ratings) {
                sum += r.getValue();
                n += 1;
            }
            final double mean = sum / n;
            logger.debug("mean rating is {}", mean);

            // TODO Use vectorz vectors instead of raw arrays
            double uoff[] = new double[snapshot.getUserIds().size()];
            double ioff[] = new double[snapshot.getItemIds().size()];

            final TrainingLoopController trainingController = stoppingCondition.newLoop();
            double rmse = 0.0;
            while (trainingController.keepTraining(rmse)) {
                double sse = 0;
                for (IndexedPreference r : ratings) {
                    final int uidx = r.getUserIndex();
                    final int iidx = r.getItemIndex();
                    final double p = mean + uoff[uidx] + ioff[iidx];
                    final double err = r.getValue() - p;
                    uoff[uidx] += learningRate * (err - regularizationFactor * Math.abs(uoff[uidx]));
                    ioff[iidx] += learningRate * (err - regularizationFactor * Math.abs(ioff[iidx]));
                    sse += err * err;
                }
                rmse = Math.sqrt(sse / ratings.size());

                logger.debug("finished iteration {} (RMSE={})", trainingController.getIterationCount(), rmse);
            }

            logger.info("trained baseline on {} ratings in {} iterations (final rmse={})", ratings.size(), trainingController.getIterationCount(), rmse);

            // Convert the uoff array to a SparseVector

            Long2DoubleMap svuoff = Long2DoubleSortedArrayMap.fromArray(snapshot.userIndex(), uoff);
            // Convert the ioff array to a SparseVector
            Long2DoubleMap svioff = Long2DoubleSortedArrayMap.fromArray(snapshot.itemIndex(), ioff);
            return new LeastSquaresItemScorer(svuoff, svioff, mean);
        }
    }
}
