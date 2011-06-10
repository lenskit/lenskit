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
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * Base class for item recommenders that use a rating predictor to generate recommendations.
 * Implements all methods required by {@link AbstractItemRecommender}.
 */
public abstract class AbstractPredictItemRecommender extends AbstractItemRecommender {
	
	protected final RatingPredictor predictor;
	
	protected AbstractPredictItemRecommender(RatingDataAccessObject dao, RatingPredictor predictor) {
		super(dao);
		this.predictor = predictor;
	}
	
	protected List<ScoredId> recommend(long user, int n, LongSet candidates, LongSet exclude) {
		if (candidates == null)
			candidates = getPredictableItems(user);
		if (!exclude.isEmpty())
			candidates = LongSortedArraySet.setDifference(candidates, exclude);

		SparseVector predictions = predictor.predict(user, candidates);
		assert(SparseVector.isComplete(predictions));
		if (predictions.isEmpty()) return Collections.emptyList();
		PriorityQueue<ScoredId> queue = new PriorityQueue<ScoredId>(predictions.size());
		for (Long2DoubleMap.Entry pred: predictions.fast()) {
			final double v = pred.getDoubleValue();
			if (!Double.isNaN(v)) {
				queue.add(new ScoredId(pred.getLongKey(), v));
			}
		}

		ScoredId[] finalPredictions;
		if (n < 0 || n > queue.size()) {
			finalPredictions = new ScoredId[queue.size()];
		} else {
			finalPredictions = new ScoredId[n];
			for (int i = queue.size() - n; i > 0; i--) queue.poll();
		}
		for (int i = finalPredictions.length-1; i >= 0; i--) {
			finalPredictions[i] = queue.poll();
		}

		return Arrays.asList(finalPredictions);
	}
	
	/**
	 * Determine the items for which predictions can be made for a certain user.
	 * @param user The user's ID.
	 * @return All items for which predictions can be generated for the user.
	 */
	protected abstract LongSet getPredictableItems(long user);

}
