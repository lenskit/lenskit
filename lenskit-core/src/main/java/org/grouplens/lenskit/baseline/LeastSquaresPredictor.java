/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.iterative.StoppingCondition;
import org.grouplens.lenskit.iterative.params.LearningRate;
import org.grouplens.lenskit.iterative.params.RegularizationTerm;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.VectorEntry.State;


/**
 * Generate baseline predictions with regularization
 *
 * @author Ark Xu <xuxxx728@umn.edu>
 */
public class LeastSquaresPredictor extends AbstractBaselinePredictor implements Serializable {
        private final MutableSparseVector userOffsets;
        private final MutableSparseVector itemOffsets;
        private final double mean;

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
                for (VectorEntry e: output.fast(state)) {
                    final long item = e.getKey();
                    double score = mean + userOffsets.get(user) + itemOffsets.get(item);
                    output.set(e, score);
                }
        }

        /**
         *  A builder that creates a regularizationFactor
         */
        public static class Builder implements Provider<LeastSquaresPredictor> {
                private final double learningRate;
                private final double regularizationFactor;
                private final double mean;
                
                private PreferenceSnapshot snapshot;
                private StoppingCondition trainingStop;
                
                /**
                 * Create a new builder
                 * 
                 * @param data
                 */
                @Inject
                public Builder(@RegularizationTerm double regFactor, @LearningRate double lrate, @Transient PreferenceSnapshot data,
                			   StoppingCondition stop) {
                        this.regularizationFactor = regFactor;
                        this.learningRate = lrate;
                        this.snapshot = data;
                        this.trainingStop = stop;

                        double sum = 0.0;
                        FastCollection<IndexedPreference> n = data.getRatings();
                        for (IndexedPreference r : CollectionUtils.fast(n)) {
                                sum += r.getValue();
                        }
                        mean = sum / n.size();
                }
                
                @Override
                public LeastSquaresPredictor get() {
                    	double rmse = Double.MAX_VALUE;
                    	double oldRmse = 0.0;
                    	double uoff[] = new double[snapshot.getUserIds().size()];
                    	double ioff[] = new double[snapshot.getItemIds().size()];
                    	FastCollection<IndexedPreference> ratings = snapshot.getRatings();
                    	
                        int niters = 0;
                        while (!trainingStop.isFinished(niters, oldRmse - rmse)) {
                                ++niters;
                                double sse = 0;
                                for (IndexedPreference r : CollectionUtils.fast(ratings)) {
                                        final int uidx = r.getUserIndex();
                                        final int iidx = r.getItemIndex(); 
                                        final double p = mean + uoff[uidx] + ioff[iidx];
                                        final double err = r.getValue() - p;
                                        uoff[uidx] += learningRate * (err - regularizationFactor*uoff[uidx]);
                                        ioff[iidx] += learningRate * (err - regularizationFactor*ioff[iidx]);
                                        sse += err*err;
                                }
                                oldRmse = rmse;
                                rmse = Math.sqrt(sse/ratings.size());
                        }
                        
                        // Convert the uoff array to a SparseVector
                        MutableSparseVector svuoff = new MutableSparseVector(snapshot.getUserIds());
                        for (VectorEntry e: svuoff.fast(State.EITHER)) {
                                final long k = e.getKey();
                                final int uid = snapshot.userIndex().getIndex(k);
                                svuoff.set(e, uoff[uid]);
                        }
                        
                        // Convert the ioff array to a SparseVector
                        MutableSparseVector svioff = new MutableSparseVector(snapshot.getItemIds());
                        for (VectorEntry e: svioff.fast(State.EITHER)) {
                                final long k = e.getKey();
                                final int iid = snapshot.itemIndex().getIndex(k);
                                svioff.set(e, ioff[iid]);
                        }
                        
                        return new LeastSquaresPredictor(svuoff, svioff, mean);
                }
        }
}
