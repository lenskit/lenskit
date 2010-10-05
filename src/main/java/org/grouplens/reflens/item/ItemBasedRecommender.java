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

import it.unimi.dsi.fastutil.ints.AbstractIntComparator;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.RatingVectorFactory;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.NeighborhoodSize;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.util.IndexedItemScore;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixFactory;

import com.google.inject.Inject;

public class ItemBasedRecommender<U,I> implements Recommender<U,I> {
	private final Normalization<RatingVector<U,I>> ratingNormalizer;
	private final Similarity<RatingVector<I,U>> itemSimilarity;
	private final int neighborhoodSize;
	
	private final SimilarityMatrixFactory matrixFactory;
	private final RatingVectorFactory<I,U> itemVectorFactory;
	private final Indexer<I> itemIndexer;
	
	private SimilarityMatrix matrix;

	@Inject
	ItemBasedRecommender(
			Indexer<I> itemIndexer,
			RatingVectorFactory<I, U> itemVectorFactory,
			SimilarityMatrixFactory matrixFactory,
			@RatingNormalization Normalization<RatingVector<U,I>> ratingNormalizer,
			@ItemSimilarity Similarity<RatingVector<I,U>> itemSimilarity,
			@NeighborhoodSize int neighborhoodSize) {
		this.neighborhoodSize = neighborhoodSize;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
		this.itemVectorFactory = itemVectorFactory;
		this.itemIndexer = itemIndexer;
		this.matrixFactory = matrixFactory;
	}

	/** 
	 * Build the item-item similarity model from a list of user rating
	 * histories.
	 * @param ratings
	 */
	void buildModel(Collection<RatingVector<U,I>> ratings) {
		List<RatingVector<I,U>> itemRatings = buildItemRatings(ratings);
		
		// prepare the similarity matrix
		matrix = matrixFactory.create(itemRatings.size());
		
		// compute the similarity matrix
		if (itemSimilarity instanceof SymmetricBinaryFunction) {
			// we can compute equivalent symmetries at the same time
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = i+1; j < itemRatings.size(); j++) {
					float sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0) {
						matrix.put(i, j, sim);
						matrix.put(j, i, sim);
					}
				}
			}
		} else {
			// less efficient route
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = 0; j < itemRatings.size(); j++) {
					float sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0)
						matrix.put(i, j, sim);
				}
			}
		}
		
		matrix.finish();
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return
	 */
	private List<RatingVector<I,U>> buildItemRatings(Collection<RatingVector<U,I>> ratings) {
		ArrayList<RatingVector<I,U>> itemVectors = new ArrayList<RatingVector<I,U>>();
		for (RatingVector<U,I> user: ratings) {
			user = ratingNormalizer.normalize(user);
			for (ObjectValue<I> rating: user) {
				I item = rating.getItem();
				int idx = itemIndexer.getIndex(item);
				if (idx >= itemVectors.size()) {
					// it's a new item - add one
					assert idx == itemVectors.size();
					itemVectors.add(itemVectorFactory.make(item));
				}
				itemVectors.get(idx).putRating(user.getOwner(), rating.getRating());
			}
		}
		itemVectors.trimToSize();
		return itemVectors;
	}

	// TODO: Support multiple simultaneous predictions
	@Override
	public ObjectValue<I> predict(RatingVector<U,I> user, I item) {
		int iid = itemIndexer.getIndex(item);
		if (iid >= matrix.size())
			return null;

		float sum = 0;
		float totalWeight = 0;
		for (IndexedItemScore score: matrix.getNeighbors(iid)) {
			I other = itemIndexer.getObject(score.getIndex());
			float s = score.getScore();
			if (user.containsObject(other)) {
				float rating = user.getRating(other) - user.getAverage();
				sum += rating * s;
				totalWeight += Math.abs(s);
			}
		}
		if (totalWeight >= 0.1) {
			return new ObjectValue<I>(item, sum / totalWeight + user.getAverage());
		} else {
			return null;
		}
	}

	@Override
	public List<ObjectValue<I>> recommend(RatingVector<U,I> user) {
		Int2FloatMap scores = new Int2FloatOpenHashMap();
		Int2FloatMap weights = new Int2FloatOpenHashMap();
		float avg = user.getAverage();
		for (ObjectValue<I> rating: user) {
			int iid = itemIndexer.getIndex(rating.getItem());
			if (iid >= matrix.size())
				continue;
			for (IndexedItemScore score: matrix.getNeighbors(iid)) {
				int jid = score.getIndex();
				float val = score.getScore();
				if (!user.containsObject(itemIndexer.getObject(jid))) {
					float s = 0.0f;
					float w = 0.0f;
					if (scores.containsKey(jid)) {
						s = scores.get(jid);
						w = weights.get(jid);
					}
					s += val * (rating.getRating() - avg);
					w += Math.abs(val);
					scores.put(jid, s);
					weights.put(jid, w);
				}
			}
		}
		ArrayList<ObjectValue<I>> results = new ArrayList<ObjectValue<I>>(scores.size());
		IntIterator iids = scores.keySet().iterator();
		while (iids.hasNext()) {
			int iid = iids.next();
			float w = weights.get(iid);
			if (w >= 0.1) {
				I item = itemIndexer.getObject(iid);
				float pred = scores.get(iid) / w;
				results.add(new ObjectValue<I>(item, pred + avg));
			}
		}
		Collections.sort(results);
		return results;
	}

	@Override
	public List<ObjectValue<I>> recommend(Set<I> basket) {
		// TODO Auto-generated method stub
		return null;
	}
}
