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

package org.grouplens.lenskit;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Cursors2;
import org.grouplens.lenskit.data.ScoredLongArrayList;
import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;
import org.grouplens.lenskit.util.ScoredItemAccumulator;

/**
 * Base class for item recommenders that use a rating predictor to generate
 * recommendations. Implements all methods required by
 * {@link AbstractItemRecommender}.  The default exclude set is all items rated
 * by the user.
 */
public class PredictorBasedItemRecommender extends AbstractItemRecommender {
	
	protected final RatingPredictor predictor;
	protected final DataAccessObject dao;
	
	protected PredictorBasedItemRecommender(DataAccessObject dao, RatingPredictor predictor) {
		this.predictor = predictor;
		this.dao = dao;
	}
	
	/**
	 * Implement the primary recommend method in terms of the predictor.  This
	 * method uses {@link #getDefaultExcludes(long)} to supply a missing exclude
	 * set.
	 */
	@Override
    protected ScoredLongList recommend(long user, int n, LongSet candidates, LongSet exclude) {
		if (candidates == null)
			candidates = getPredictableItems(user);
		if (exclude == null)
		    exclude = getDefaultExcludes(user);
		if (!exclude.isEmpty())
			candidates = LongSortedArraySet.setDifference(candidates, exclude);

		SparseVector predictions = predictor.predict(user, candidates);
		assert(predictions.isComplete());
		if (predictions.isEmpty()) return new ScoredLongArrayList();
		
		if (n < 0) n = predictions.size();
		ScoredItemAccumulator accum = new ScoredItemAccumulator(n);
		for (Long2DoubleMap.Entry pred: predictions.fast()) {
			final double v = pred.getDoubleValue();
			if (!Double.isNaN(v)) {
			    accum.put(pred.getLongKey(), v);
			}
		}
		
		return accum.finish();
	}
	
	/**
	 * Get the default exclude set for a user.  The base implementation gets
	 * all their rated items.
	 * 
	 * @param user The user ID.
	 * @return The set of items to exclude.
	 */
	protected LongSet getDefaultExcludes(long user) {
	    LongSet excludes = new LongOpenHashSet();
	    Cursor<? extends Rating> ratings = dao.getUserEvents(user, Rating.class);
	    try {
	        for (Rating r: ratings) {
	            excludes.add(r.getItemId());
	        }
	    } finally {
	        ratings.close();
	    }
	    return excludes;
	}
	
	/**
     * Determine the items for which predictions can be made for a certain user.
     * This implementation is naive and asks the DAO for all items; subclasses
     * should override it with something more efficient if practical.
     * 
     * @param user The user's ID.
     * @return All items for which predictions can be generated for the user.
     */
	protected LongSet getPredictableItems(long user) {
        return Cursors2.makeSet(dao.getItems());
    }

}
