/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommendationEngine;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.IndexedItemScore;

public class ItemItemRecommender implements RecommendationEngine, RatingRecommender, RatingPredictor, Serializable {
	private static final long serialVersionUID = 3157980766584927863L;
	private final ItemItemModel model;
	
	public ItemItemRecommender(ItemItemModel model) {
		this.model = model;
	}
	
	@Override
	public Map<Long,Double> predict(long user, Map<Long,Double> ratings, Collection<Long> items) {
		Int2DoubleMap sums = new Int2DoubleOpenHashMap();
		Int2DoubleMap weights = new Int2DoubleOpenHashMap();
		sums.defaultReturnValue(0);
		weights.defaultReturnValue(0);
		for (Map.Entry<Long, Double> rating: ratings.entrySet()) {
			final double r = rating.getValue();
			for (IndexedItemScore score: model.getNeighbors(rating.getKey())) {
				double s = score.getScore();
				int i = score.getIndex();
				weights.put(i, weights.get(i) + s);
				sums.put(i, sums.get(i) + s*r);
			}
		}
		Long2DoubleMap preds = new Long2DoubleOpenHashMap();
		for (long item: items) {
			int idx = model.getItemIndex(item);
			double w = weights.get(idx);
			if (w > 0) {
				preds.put(item, sums.get(idx) / w);
			}
		}
		return preds;
	}

	@Override
	public ScoredId predict(long user, Map<Long,Double> ratings, long item) {
		double sum = 0;
		double totalWeight = 0;
		for (IndexedItemScore score: model.getNeighbors(item)) {
			long other = model.getItem(score.getIndex());
			double s = score.getScore();
			if (ratings.containsKey(other)) {
				// FIXME this goes wacky with negative similarities
				double rating = ratings.get(other);
				sum += rating * s;
				totalWeight += Math.abs(s);
			}
		}
		if (totalWeight >= 0.1) {
			return new ScoredId(item, sum / totalWeight);
		} else {
			return null;
		}
	}

	@Override
	public List<ScoredId> recommend(long user, Map<Long,Double> ratings) {
		Int2DoubleMap scores = new Int2DoubleOpenHashMap();
		Int2DoubleMap weights = new Int2DoubleOpenHashMap();
		for (ScoredId rating: ScoredId.wrap(ratings.entrySet())) {
			for (IndexedItemScore score: model.getNeighbors(rating.getId())) {
				int jid = score.getIndex();
				double val = score.getScore();
				if (!ratings.containsKey(model.getItem(jid))) {
					double s = 0.0f;
					double w = 0.0f;
					if (scores.containsKey(jid)) {
						s = scores.get(jid);
						w = weights.get(jid);
					}
					// FIXME audit for behavior w/ negative similarities
					s += val * rating.getScore();
					w += Math.abs(val);
					scores.put(jid, s);
					weights.put(jid, w);
				}
			}
		}
		ArrayList<ScoredId> results = new ArrayList<ScoredId>(scores.size());
		IntIterator iids = scores.keySet().iterator();
		while (iids.hasNext()) {
			int iid = iids.next();
			double w = weights.get(iid);
			if (w >= 0.1) {
				long item = model.getItem(iid);
				double pred = scores.get(iid) / w;
				results.add(new ScoredId(item, pred));
			}
		}
		Collections.sort(results);
		return results;
	}

	@Override
	public BasketRecommender getBasketRecommender() {
		// TODO Support basket recommendations
		return null;
	}

	@Override
	public RatingPredictor getRatingPredictor() {
		return this;
	}

	@Override
	public RatingRecommender getRatingRecommender() {
		return this;
	}
}
