/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.LongIterator;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.snapshot.RatingSnapshot;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.params.MaxRating;
import org.grouplens.lenskit.params.MinRating;
import org.grouplens.lenskit.params.NormalizedSnapshot;
import org.grouplens.lenskit.slopeone.params.DeviationDamping;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * Pre-computes the deviations and number of mutual rating users for every pair
 *  of items and stores the results in a <tt>DeviationMatrix</tt> and 
 *  <tt>CoratingMatrix</tt>. These matrices are later used by a 
 *  <tt>SlopeOneRatingPredictor</tt>.
 */
public class SlopeOneModelBuilder extends RecommenderComponentBuilder<SlopeOneModel> {

	private SlopeOneModelDataAccumulator accumulator;
	private BaselinePredictor baseline;
	private double minRating;
	private double maxRating;
	private double damping;
	private RatingSnapshot normalizedData;

	/**
	 * Constructs a <tt>SlopeOneModel</tt> and the necessary matrices from
	 * a <tt>RatingSnapshot</tt>.
	 */
	@Override
	public SlopeOneModel build() {
		RatingSnapshot snap = snapshot;
		if (normalizedData != null) snap = normalizedData;
		accumulator = new SlopeOneModelDataAccumulator(damping, snap);
		for (long currentUser : snap.getUserIds()) {
			SparseVector ratings = Ratings.userRatingVector(snap.getUserRatings(currentUser));
			LongIterator iter = ratings.keySet().iterator();
			while (iter.hasNext()) {
				long item1 = iter.next();
				LongIterator iter2 = ratings.keySet().tailSet(item1).iterator();
				if (iter2.hasNext()) iter2.next();
				while (iter2.hasNext()) {
					long item2 = iter2.next();
					accumulator.putRatingPair(item1, ratings.get(item1), item2, ratings.get(item2));
				}
			}
		}
		LongSortedArraySet items = new LongSortedArraySet(snap.getItemIds());
		return new SlopeOneModel(accumulator.buildCoratingMatrix(), accumulator.buildDeviationMatrix(), baseline, items, minRating, maxRating);
	}

	/**
	 * @param predictor The <tt>BaselinePredictor</tt> to be included in the constructed <tt>SlopeOneModel</tt>
	 */
	public void setBaselinePredictor(BaselinePredictor predictor) {
		baseline = predictor;
	}

	/**
	 * @param min The lowest valid rating in a system.
	 */
	public void setMinRating(@MinRating double min) {
		minRating = min;
	}

	/**
	 * @param max The highest valid rating in a system.
	 */
	public void setMaxRating(@MaxRating double max) {
		maxRating = max;
	}

	/**
	 * @param damping A damping turn to use for deviation calculations.
	 */
	public void setDamping(@DeviationDamping double damping) {
		this.damping = damping;
	}
	
	/**
	 * @param data A <tt>RatingSnapshot</tt> containing normalized data.
	 */
	@NormalizedSnapshot
	public void setNormalizedData(RatingSnapshot data) {
		normalizedData = data;
	}
}