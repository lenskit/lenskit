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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.lenskit.AbstractRatingRecommender;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * A <tt>RatingRecommender</tt> that uses the Slope One algorithm.
 */
public class SlopeOneRatingRecommender extends AbstractRatingRecommender{

	private SlopeOneRatingPredictor predictor; 
	
	/**
     * Construct a new recommender from a predictor.
     * @param predictor The predictor to use.
     */
    public SlopeOneRatingRecommender(SlopeOneRatingPredictor predictor) {
        this.predictor = predictor;
    }
	
	@Override
    protected List<ScoredId> recommend(long user, SparseVector ratings, int n,
            LongSet candidates, LongSet exclude) {
        if (candidates == null)
            candidates = predictor.getPredictableItems(user, ratings);
        if (!exclude.isEmpty())
            candidates = LongSortedArraySet.setDifference(candidates, exclude);
        
        SparseVector predictions = predictor.predict(user, ratings, candidates);
        PriorityQueue<ScoredId> queue = new PriorityQueue<ScoredId>(predictions.size());
        for (Long2DoubleMap.Entry pred: predictions.fast()) {
            final double v = pred.getDoubleValue();
            if (!Double.isNaN(v)) {
                queue.add(new ScoredId(pred.getLongKey(), v));
            }
        }

        ArrayList<ScoredId> finalPredictions =
            new ArrayList<ScoredId>(n >= 0 ? n : queue.size());
        for (int i = 0; !queue.isEmpty() && (n < 0 || i < n); i++) {
            finalPredictions.add(queue.poll());
        }
        return finalPredictions;
	}
}
