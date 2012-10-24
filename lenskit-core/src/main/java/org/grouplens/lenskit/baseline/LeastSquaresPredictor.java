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

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.util.iterative.StoppingCondition;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.VectorEntry.State;

/**
 * Generate baseline predictions with regularization
 *
 * @author Ark Xu <xuxxx728@umn.edu>
 */
@SuppressWarnings("serial")
public class LeastSquaresPredictor extends AbstractBaselinePredictor {
	MutableSparseVector uoff;
	MutableSparseVector ioff;
	
	/**
	 * Construct a new LeastSquaresPredictor
	 * 
	 * @param uoff the user's mean offset
	 * @param ioff the item's mean offset
	 */
	public LeastSquaresPredictor(MutableSparseVector uoff, MutableSparseVector ioff) {
		this.uoff = uoff;
		this.ioff = ioff;
	}
	
	@Override
	public void predict(long user, SparseVector ratings,
			MutableSparseVector output, boolean predictSet) {
		State state = predictSet ? State.EITHER : State.UNSET;
		for (VectorEntry e: ratings.fast(state)) {
			final long item = e.getKey(); 
			double scoure = ratings.mean() + uoff.get(user) + ioff.get(item);
			output.set(e, scoure);
		}
	}
	
	/**
	 *  A builder that creates LeastSquaresPredictor
	 */
	public static class Builder implements Provider<LeastSquaresPredictor> {
		private double learningRate;
		private double lambda;
		private double mean;
		private double rmse;
		private double oldRmse;
		private double uoff[];
		private double ioff[];
		private FastCollection<IndexedPreference> ratings;
		private PreferenceSnapshot snapshot;
		private StoppingCondition trainingStop;
		
		/**
		 * Create a new builder
		 * 
		 * @param data
		 */
		@Inject
		public Builder(@Transient PreferenceSnapshot data) {
			learningRate = 0.005;
			lambda = 0.002;
			rmse = Double.MAX_VALUE;
			oldRmse = 0.0;
			
			this.snapshot = data;
			uoff = new double[snapshot.getUserIds().size()];
			ioff = new double[snapshot.getItemIds().size()];
			ratings = snapshot.getRatings();
			
			mean = 0.0;
			for (IndexedPreference r : CollectionUtils.fast(ratings)) {
				mean += r.getValue();
			}
			mean /= ratings.size();
		}
		
		@Override
		public LeastSquaresPredictor get() {
			int niters = 0;
			while (!trainingStop.isFinished(niters, oldRmse - rmse)) {
				++niters;
				double sse = 0;
				for (IndexedPreference r : CollectionUtils.fast(ratings)) {
					final long user = r.getUserId();
					final long item = r.getItemId();
					final double p = predict(user, item);
					final double err = r.getValue() - p;
					final int uid = snapshot.userIndex().getIndex(user);
					final int iid = snapshot.itemIndex().getIndex(item); 
					uoff[uid] += learningRate * (err - lambda*uoff[uid]);
					ioff[iid] += learningRate * (err - lambda*ioff[iid]);
					sse += err*err;
				}
				rmse = Math.sqrt(sse/ratings.size());
			}
			
			// Convert the uoff array to a SparseVector
			MutableSparseVector svuoff = new MutableSparseVector(snapshot.getUserIds());
			for (VectorEntry e: svuoff.fast()) {
				final long k = e.getKey();
				final int uid = snapshot.userIndex().getIndex(k);
				svuoff.set(e, uoff[uid]);
			}
			
			// Convert the ioff array to a SparseVector
			MutableSparseVector svioff = new MutableSparseVector(snapshot.getItemIds());
			for (VectorEntry e: svioff.fast()) {
				final long k = e.getKey();
				final int iid = snapshot.itemIndex().getIndex(k);
				svioff.set(e, ioff[iid]);
			}
			
			return new LeastSquaresPredictor(svuoff, svioff);
		}
		
		/**
		 * Generate the current prediction
		 * @param user the user's id
		 * @param item the item's id
		 * @return the current prediction for the (user, item)  
		 */
		public double predict(long user, long item) {
			final int uid = snapshot.userIndex().getIndex(user);
			final int iid = snapshot.itemIndex().getIndex(item);
			return mean + uoff[uid] + ioff[iid];
		}
	}
}
