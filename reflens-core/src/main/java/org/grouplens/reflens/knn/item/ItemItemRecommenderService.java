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

package org.grouplens.reflens.knn.item;

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.RecommenderService;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.vector.MutableSparseVector;
import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.util.IndexedItemScore;
import org.grouplens.reflens.util.LongSortedArraySet;

/**
 * Generate predictions and recommendations using item-item CF.
 *
 * This class implements an item-item collaborative filter backed by a particular
 * {@link ItemItemModel}.  Client code will usually use a
 * {@link RecommenderBuilder} to get one of these.
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
public class ItemItemRecommenderService implements RecommenderService, RatingRecommender, RatingPredictor, Serializable {
	private static final long serialVersionUID = 3157980766584927863L;
	protected final @Nonnull ItemItemModel model;

	/**
	 * Construct a new recommender from an item-item recommender model.
	 * @param model The backing model for the new recommender.
	 */
	public ItemItemRecommenderService(@Nonnull ItemItemModel model) {
		this.model = model;
	}

	@Override
	public ScoredId predict(long user, SparseVector ratings, long item) {
		MutableSparseVector normed = MutableSparseVector.copy(ratings);
		model.subtractBaseline(user, ratings, normed);
		double sum = 0;
		double totalWeight = 0;
		for (IndexedItemScore score: model.getNeighbors(item)) {
			long other = model.getItem(score.getIndex());
			double s = score.getScore();
			if (normed.containsId(other)) {
				// FIXME this goes wacky with negative similarities
				double rating = normed.get(other);
				sum += rating * s;
				totalWeight += abs(s);
			}
		}
		double pred = 0;
		if (totalWeight > 0)
			pred = sum / totalWeight;
		// FIXME Should return NULL if there is no baseline
		return new ScoredId(item, model.addBaseline(user, ratings, item, pred));
	}

	@Override
	public SparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
		MutableSparseVector normed = MutableSparseVector.copy(ratings);
		model.subtractBaseline(user, ratings, normed);

		LongSortedSet iset;
		if (items instanceof LongSortedSet)
			iset = (LongSortedSet) items;
		else
			iset = new LongSortedArraySet(items);

		MutableSparseVector sums = new MutableSparseVector(iset);
		MutableSparseVector weights = new MutableSparseVector(iset);
		for (Long2DoubleMap.Entry rating: normed.fast()) {
			final double r = rating.getDoubleValue();
			for (IndexedItemScore score: model.getNeighbors(rating.getLongKey())) {
				final double s = score.getScore();
				final int idx = score.getIndex();
				final long iid = model.getItem(idx);
				weights.add(iid, abs(s));
				sums.add(iid, s*r);
			}
		}

		final boolean hasBaseline = model.hasBaseline();
		LongIterator iter = sums.keySet().iterator();
		while (iter.hasNext()) {
			final long iid = iter.next();
			final double w = weights.get(iid);
			if (w > 0)
				sums.set(iid, sums.get(iid) / w);
			else
				sums.set(iid, hasBaseline ? 0 : Double.NaN);
		}

		model.addBaseline(user, ratings, sums);
		return sums;
	}

	LongSet getRecommendableItems(long user, SparseVector ratings) {
		if (model.hasBaseline()) {
			return model.getItemUniverse();
		} else {
			LongSet items = new LongOpenHashSet();
			LongIterator iter = ratings.keySet().iterator();
			while (iter.hasNext()) {
				final long item = iter.nextLong();
				for (IndexedItemScore n: model.getNeighbors(item)) {
					items.add(model.getItem(n.getIndex()));
				}
			}
			return items;
		}
	}

	@Override
	public List<ScoredId> recommend(long user, SparseVector ratings) {
		return recommend(user, ratings, -1, null);
	}

	@Override
	public List<ScoredId> recommend(long user, SparseVector ratings, Set<Long> candidates) {
		return recommend(user, ratings, -1, candidates);
	}

	@Override
	public List<ScoredId> recommend(long user, SparseVector ratings, int n, Set<Long> candidates) {
		if (candidates == null)
			candidates = getRecommendableItems(user, ratings);
		SparseVector predictions = predict(user, ratings, candidates);
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
