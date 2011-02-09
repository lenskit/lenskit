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

package org.grouplens.reflens.knn;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommenderEngine;
import org.grouplens.reflens.RecommenderEngineBuilder;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.IndexedItemScore;

/**
 * Generate predictions and recommendations using item-item CF.
 * 
 * This class implements an item-item collaborative filter backed by a particular
 * {@link ItemItemModel}.  Client code will usually use a
 * {@link RecommenderEngineBuilder} to get one of these.
 * 
 * To modify the recommendation or prediction logic, do the following:
 * 
 * <ul>
 * <li>Extend {@link ItemItemRecommenderBuilder}, reimplementing the
 * {@link ItemItemRecommenderBuilder#createRecommender(ItemItemModel)} method
 * to create an instance of your new class rather than this one.
 * <li>Configure Guice to inject your new recommender builder.
 * </ul>
 * 
 * @todo Document how this class works.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
public class ItemItemRecommender implements RecommenderEngine, RatingRecommender, RatingPredictor, Serializable {
	private static final long serialVersionUID = 3157980766584927863L;
	protected final @Nonnull ItemItemModel model;
	
	/**
	 * Construct a new recommender from an item-item recommender model.
	 * @param model The backing model for the new recommender.
	 */
	public ItemItemRecommender(@Nonnull ItemItemModel model) {
		this.model = model;
	}
	
	@Override
	public ScoredId predict(long user, RatingVector ratings, long item) {
		RatingVector normed = model.subtractBaseline(user, ratings);
		double sum = 0;
		double totalWeight = 0;
		for (IndexedItemScore score: model.getNeighbors(item)) {
			long other = model.getItem(score.getIndex());
			double s = score.getScore();
			if (normed.containsId(other)) {
				// FIXME this goes wacky with negative similarities
				double rating = normed.get(other);
				sum += rating * s;
				totalWeight += Math.abs(s);
			}
		}
		double pred = 0;
		if (totalWeight > 0)
			pred = sum / totalWeight;
		// FIXME Should return NULL if there is no baseline
		return new ScoredId(item, model.addBaseline(user, ratings, item, pred));
	}
	
	@Override
	public RatingVector predict(long user, RatingVector ratings, Collection<Long> items) {
		RatingVector normed = model.subtractBaseline(user, ratings); 
		Int2DoubleMap sums = new Int2DoubleOpenHashMap();
		Int2DoubleMap weights = new Int2DoubleOpenHashMap();
		sums.defaultReturnValue(0);
		weights.defaultReturnValue(0);
		for (Long2DoubleMap.Entry rating: normed.fast()) {
			final double r = rating.getValue();
			for (IndexedItemScore score: model.getNeighbors(rating.getKey())) {
				double s = score.getScore();
				int i = score.getIndex();
				weights.put(i, weights.get(i) + s);
				sums.put(i, sums.get(i) + s*r);
			}
		}
		RatingVector preds = new RatingVector(items.size());
		for (long item: items) {
			final int idx = model.getItemIndex(item);
			final double w = weights.get(idx);
			double p = 0;
			if (w > 0)
				p = sums.get(idx) / w;
			preds.put(item, p);
		}
		return model.addBaseline(user, ratings, preds);
	}

	@Override
	public List<ScoredId> recommend(long user, RatingVector ratings) {
		Int2DoubleMap scores = new Int2DoubleOpenHashMap();
		Int2DoubleMap weights = new Int2DoubleOpenHashMap();
		for (Long2DoubleMap.Entry rating: ratings.fast()) {
			for (IndexedItemScore score: model.getNeighbors(rating.getKey())) {
				int jid = score.getIndex();
				double val = score.getScore();
				if (!ratings.containsId(model.getItem(jid))) {
					double s = 0.0f;
					double w = 0.0f;
					if (scores.containsKey(jid)) {
						s = scores.get(jid);
						w = weights.get(jid);
					}
					// FIXME audit for behavior w/ negative similarities
					s += val * rating.getValue();
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
