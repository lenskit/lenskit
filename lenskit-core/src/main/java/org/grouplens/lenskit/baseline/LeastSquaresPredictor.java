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
package org.grouplens.lenskit.baseline;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.LearningRate;
import org.grouplens.lenskit.iterative.RegularizationTerm;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.VectorEntry.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;


/**
 * Generate baseline predictions with regularization.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(LeastSquaresPredictor.Builder.class)
@Shareable
public class LeastSquaresPredictor extends AbstractBaselinePredictor implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ImmutableSparseVector userOffsets;
    private final ImmutableSparseVector itemOffsets;
    private final double mean;

    private static final Logger logger = LoggerFactory.getLogger(LeastSquaresPredictor.class);

    /**
     * Construct a new LeastSquaresPredictor.
     *
     * @param uoff the user offsets
     * @param ioff the item offsets
     * @param mean the global mean rating
     */
    public LeastSquaresPredictor(ImmutableSparseVector uoff, ImmutableSparseVector ioff, double mean) {
        this.userOffsets = uoff;
        this.itemOffsets = ioff;
        this.mean = mean;
    }

    @Override
    public void predict(long user, MutableSparseVector output, boolean predictSet) {
        State state = predictSet ? State.EITHER : State.UNSET;
        for (VectorEntry e : output.fast(state)) {
            double score = mean + userOffsets.get(user, 0) + itemOffsets.get(e.getKey(), 0);
            output.set(e, score);
        }
    }

    /**
     * The builder for the least squares predictor.
     */
    public static class Builder implements Provider<LeastSquaresPredictor> {
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
        public LeastSquaresPredictor get() {
            double rmse = 0.0;
            double uoff[] = new double[snapshot.getUserIds().size()];
            double ioff[] = new double[snapshot.getItemIds().size()];
            FastCollection<IndexedPreference> ratings = snapshot.getRatings();

            logger.debug("training predictor on {} ratings", ratings.size());

            double sum = 0.0;
            double n = 0;
            for (IndexedPreference r : CollectionUtils.fast(ratings)) {
                sum += r.getValue();
                n += 1;
            }
            final double mean = sum / n;
            logger.debug("mean rating is {}", mean);

            final TrainingLoopController trainingController = stoppingCondition.newLoop();
            while (trainingController.keepTraining(rmse)) {
                double sse = 0;
                for (IndexedPreference r : CollectionUtils.fast(ratings)) {
                    final int uidx = r.getUserIndex();
                    final int iidx = r.getItemIndex();
                    final double p = mean + uoff[uidx] + ioff[iidx];
                    final double err = r.getValue() - p;
                    uoff[uidx] += learningRate * (err - regularizationFactor * uoff[uidx]);
                    ioff[iidx] += learningRate * (err - regularizationFactor * ioff[iidx]);
                    sse += err * err;
                }
                rmse = Math.sqrt(sse / ratings.size());

                logger.debug("finished iteration {} (RMSE={})", trainingController.getIterationCount(), rmse);
            }

            logger.info("trained baseline on {} ratings in {} iterations (final rmse={})", ratings.size(), trainingController.getIterationCount(), rmse);

            // Convert the uoff array to a SparseVector
            MutableSparseVector svuoff = snapshot.userIndex().convertArrayToVector(uoff);
            // Convert the ioff array to a SparseVector
            MutableSparseVector svioff = snapshot.itemIndex().convertArrayToVector(ioff);
            return new LeastSquaresPredictor(svuoff.freeze(), svioff.freeze(), mean);
        }
    }
}
