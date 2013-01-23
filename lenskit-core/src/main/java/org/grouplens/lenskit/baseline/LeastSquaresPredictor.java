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
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.TrainingLoopController;
import org.grouplens.lenskit.iterative.params.LearningRate;
import org.grouplens.lenskit.iterative.params.RegularizationTerm;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.VectorEntry.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.Serializable;


/**
 * Generate baseline predictions with regularization
 *
 * @author Ark Xu <xuxxx728@umn.edu>
 */
@DefaultProvider(LeastSquaresPredictor.Builder.class)
public class LeastSquaresPredictor extends AbstractBaselinePredictor implements Serializable {
    private final MutableSparseVector userOffsets;
    private final MutableSparseVector itemOffsets;
    private final double mean;

    private static final Logger logger = LoggerFactory.getLogger(LeastSquaresPredictor.class);

    /**
     * Construct a new LeastSquaresPredictor
     *
     * @param uoff the user offsets
     * @param ioff the item offsets
     * @param mean the global mean rating
     */
    public LeastSquaresPredictor(MutableSparseVector uoff, MutableSparseVector ioff, double mean) {
        this.userOffsets = uoff;
        this.itemOffsets = ioff;
        this.mean = mean;
    }

    @Override
    public void predict(long user, SparseVector ratings,
                        MutableSparseVector output, boolean predictSet) {
        State state = predictSet ? State.EITHER : State.UNSET;
        for (VectorEntry e : output.fast(state)) {
            double score = mean + userOffsets.get(user, 0) + itemOffsets.get(e.getKey(), 0);
            output.set(e, score);
        }
    }

    /**
     * A builder that creates a regularizationFactor
     */
    public static class Builder implements Provider<LeastSquaresPredictor> {
        private final double learningRate;
        private final double regularizationFactor;
        private final double mean;
        private PreferenceSnapshot snapshot;
        private TrainingLoopController trainingController;

        /**
         * Create a new builder
         *
         * @param data
         */
        @Inject
        public Builder(@RegularizationTerm double regFactor, @LearningRate double lrate, @Transient PreferenceSnapshot data,
                       TrainingLoopController controller) {
            regularizationFactor = regFactor;
            learningRate = lrate;
            snapshot = data;
            trainingController = controller;

            double sum = 0.0;
            FastCollection<IndexedPreference> n = data.getRatings();
            for (IndexedPreference r : CollectionUtils.fast(n)) {
                sum += r.getValue();
            }
            mean = sum / n.size();
        }

        @Override
        public LeastSquaresPredictor get() {
            double rmse = 0.0;
            double uoff[] = new double[snapshot.getUserIds().size()];
            double ioff[] = new double[snapshot.getItemIds().size()];
            FastCollection<IndexedPreference> ratings = snapshot.getRatings();

            logger.debug("training predictor on {} ratings", ratings.size());

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
            MutableSparseVector svuoff = new MutableSparseVector(snapshot.getUserIds());
            for (VectorEntry e : svuoff.fast(State.EITHER)) {
                final int uid = snapshot.userIndex().getIndex(e.getKey());
                svuoff.set(e, uoff[uid]);
            }

            // Convert the ioff array to a SparseVector
            MutableSparseVector svioff = new MutableSparseVector(snapshot.getItemIds());
            for (VectorEntry e : svioff.fast(State.EITHER)) {
                final int iid = snapshot.itemIndex().getIndex(e.getKey());
                svioff.set(e, ioff[iid]);
            }

            return new LeastSquaresPredictor(svuoff, svioff, mean);
        }
    }
}
