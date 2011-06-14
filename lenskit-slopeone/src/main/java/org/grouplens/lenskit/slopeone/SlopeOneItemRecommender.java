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
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.grouplens.lenskit.AbstractDynamicPredictItemRecommender;
import org.grouplens.lenskit.data.ScoredId;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * A <tt>RatingRecommender</tt> that uses the Slope One algorithm.
 */
public class SlopeOneItemRecommender extends AbstractDynamicPredictItemRecommender {
    // FIXME Use AbstractDynamicPredictItemRecommender for this

	private SlopeOneRatingPredictor predictor; 
	
	/**
     * Construct a new recommender from a predictor.
     * @param predictor The predictor to use.
     */
    public SlopeOneItemRecommender(RatingDataAccessObject dao, SlopeOneRatingPredictor predictor) {
        super(dao, predictor);
        this.predictor = predictor;
    }
	
	@Override
    protected List<ScoredId> recommend(long user, SparseVector ratings, int n,
            LongSet candidates, LongSet exclude) {
		if (candidates == null)
            candidates = getPredictableItems(user, ratings);
        if (!exclude.isEmpty())
            candidates = LongSortedArraySet.setDifference(candidates, exclude);
        SparseVector predictions = predictor.predict(user, ratings, candidates);
        assert(predictions.isComplete());
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
        for (int i = finalPredictions.length - 1; i >= 0; i--) {
            finalPredictions[i] = queue.poll();
        }
        return Arrays.asList(finalPredictions);
    }
	
	@Override
	protected LongSet getPredictableItems(long user, SparseVector ratings) {
		if (predictor.getModel().getBaselinePredictor() != null) return predictor.getModel().getItemUniverse();
		else {
			LongSet predictable = new LongOpenHashSet();
			for (long id1 : predictor.getModel().getItemUniverse()) {
				LongIterator iter = ratings.keySet().iterator();
				int nusers = 0;
				while (iter.hasNext() && nusers == 0) {
					nusers += predictor.getModel().getCoratings(id1, iter.next());
				}
				if (nusers > 0) predictable.add(id1);
			}
			return predictable;
		}		
	}
}
