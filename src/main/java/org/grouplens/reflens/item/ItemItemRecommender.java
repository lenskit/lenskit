/* RefLens, a reference implementation of recommender algorithms.
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
 */

package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommendationEngine;
import org.grouplens.reflens.data.ScoredObject;
import org.grouplens.reflens.util.IndexedItemScore;

public class ItemItemRecommender<U,I> implements RecommendationEngine<U,I>, RatingRecommender<U,I>, RatingPredictor<U,I>, Serializable {
	private static final long serialVersionUID = 3157980766584927863L;
	private final ItemItemModel<U,I> model;
	
	public ItemItemRecommender(ItemItemModel<U,I> model) {
		this.model = model;
	}

	@Override
	public ScoredObject<I> predict(U user, Map<I,Float> ratings, I item) {
		float sum = 0;
		float totalWeight = 0;
		for (IndexedItemScore score: model.getNeighbors(item)) {
			I other = model.getItem(score.getIndex());
			float s = score.getScore();
			if (ratings.containsKey(other)) {
				// FIXME this goes wacky with negative similarities
				float rating = ratings.get(other);
				sum += rating * s;
				totalWeight += Math.abs(s);
			}
		}
		if (totalWeight >= 0.1) {
			return new ScoredObject<I>(item, sum / totalWeight);
		} else {
			return null;
		}
	}

	@Override
	public List<ScoredObject<I>> recommend(U user, Map<I,Float> ratings) {
		Int2FloatMap scores = new Int2FloatOpenHashMap();
		Int2FloatMap weights = new Int2FloatOpenHashMap();
		for (ScoredObject<I> rating: ScoredObject.wrap(ratings.entrySet())) {
			for (IndexedItemScore score: model.getNeighbors(rating.getObject())) {
				int jid = score.getIndex();
				float val = score.getScore();
				if (!ratings.containsKey(model.getItem(jid))) {
					float s = 0.0f;
					float w = 0.0f;
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
		ArrayList<ScoredObject<I>> results = new ArrayList<ScoredObject<I>>(scores.size());
		IntIterator iids = scores.keySet().iterator();
		while (iids.hasNext()) {
			int iid = iids.next();
			float w = weights.get(iid);
			if (w >= 0.1) {
				I item = model.getItem(iid);
				float pred = scores.get(iid) / w;
				results.add(new ScoredObject<I>(item, pred));
			}
		}
		Collections.sort(results);
		return results;
	}

	@Override
	public BasketRecommender<U, I> getBasketRecommender() {
		// TODO Support basket recommendations
		return null;
	}

	@Override
	public RatingPredictor<U, I> getRatingPredictor() {
		return this;
	}

	@Override
	public RatingRecommender<U, I> getRatingRecommender() {
		return this;
	}
}
