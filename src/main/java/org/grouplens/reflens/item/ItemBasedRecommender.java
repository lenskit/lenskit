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
import java.util.Map;
import java.util.Set;

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.DataFactory;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.UserHistory;

public class ItemBasedRecommender<U,I> implements Recommender<U,I> {
	private Normalization<UserHistory<U,I>> ratingNormalizer;
	private Similarity<RatingVector<U>> itemSimilarity;
	private Normalization<Int2FloatMap> itemSimilarityNormalizer;
	private int neighborhoodSize;
	
	private DataFactory<U, I> dataFactory;
	private Indexer<U> userIndexer;
	private Indexer<I> itemIndexer;
	
	private Int2FloatMap[] similarities;

	ItemBasedRecommender(int neighborhoodSize,
			Normalization<UserHistory<U, I>> ratingNormalizer,
			Similarity<RatingVector<U>> itemSimilarity,
			Normalization<Int2FloatMap> itemSimilarityNormalizer,
			DataFactory<U,I> dataFactory) {
		this.neighborhoodSize = neighborhoodSize;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
		this.itemSimilarityNormalizer = itemSimilarityNormalizer;
		this.dataFactory = dataFactory;
		this.userIndexer = dataFactory.makeUserIndexer();
		this.itemIndexer = dataFactory.makeItemIndexer();
	}

	/** 
	 * Build the item-item similarity model from a list of user rating
	 * histories.
	 * @param ratings
	 */
	void buildModel(Collection<UserHistory<U,I>> ratings) {
		List<RatingVector<U>> itemRatings = buildItemRatings(ratings);
		
		// prepare the similarity matrix
		similarities = new Int2FloatMap[itemRatings.size()];
		for (int i = 0; i < itemRatings.size(); i++) {
			similarities[i] = new Int2FloatOpenHashMap();
		}
		
		// compute the similarity matrix
		if (itemSimilarity.isSymmetric()) {
			// we can compute equivalent symmetries at the same time
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = i+1; j < itemRatings.size(); j++) {
					float sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0) {
						similarities[i].put(j, sim);
						similarities[j].put(i, sim);
					}
				}
			}
		} else {
			// less efficient route
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = 0; j < itemRatings.size(); j++) {
					float sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0)
						similarities[i].put(j, sim);
				}
			}
		}
		
		// Normalize and truncate similarity lists
		for (int i = 0; i < similarities.length; i++) {
			Int2FloatMap sims = similarities[i];
			// Normalize the similarity list if we have one
			if (itemSimilarityNormalizer != null)
				sims = itemSimilarityNormalizer.normalize(sims);
			
			// Truncate the similarity list if a neighborhood size is specified.
			// Use a heap for O(n lg nsims) performance.
			if (neighborhoodSize > 0) {
				IntPriorityQueue pq = new IntHeapPriorityQueue(neighborhoodSize + 1,
						new SimilarityComparator(sims));
				IntIterator iter = sims.keySet().iterator();
				while (iter.hasNext()) {
					pq.enqueue(iter.nextInt());
					while (pq.size() > neighborhoodSize) {
						pq.dequeueInt();
					}
				}
				Int2FloatMap oldSims = sims;
				sims = new Int2FloatOpenHashMap();
				while (!pq.isEmpty()) {
					int iid = pq.dequeueInt();
					sims.put(iid, oldSims.get(iid));
				}
			}
			similarities[i] = sims;
		}
	}
	
	private static class SimilarityComparator extends AbstractIntComparator {
		private Int2FloatMap sims;
		public SimilarityComparator(Int2FloatMap sims) {
			this.sims = sims;
		}
		@Override
		public int compare(int i1, int i2) {
			return Float.compare(sims.get(i1), sims.get(i2));
		}
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return
	 */
	private List<RatingVector<U>> buildItemRatings(Collection<UserHistory<U,I>> ratings) {
		ArrayList<RatingVector<U>> itemVectors = new ArrayList<RatingVector<U>>();
		for (UserHistory<U,I> user: ratings) {
			user = ratingNormalizer.normalize(user);
			for (ObjectValue<I> rating: user) {
				int idx = itemIndexer.getIndex(rating.getItem());
				if (idx > itemVectors.size()) {
					// it's a new item - add one
					assert idx == itemVectors.size();
					itemVectors.add(dataFactory.makeItemRatingVector());
				}
				itemVectors.get(idx).putRating(user.getUser(), rating.getRating());
			}
		}
		itemVectors.trimToSize();
		return itemVectors;
	}

	@Override
	public ObjectValue<I> predict(UserHistory<U,I> user, I item) {
		int iid = itemIndexer.getIndex(item);
		float sum = 0;
		float totalWeight = 0;
		for (Map.Entry<Integer, Float> entry: similarities[iid].entrySet()) {
			I other = itemIndexer.getObject(entry.getKey());
			float s = entry.getValue();
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
	public List<ObjectValue<I>> recommend(UserHistory<U,I> user) {
		Int2FloatMap scores = new Int2FloatOpenHashMap();
		Int2FloatMap weights = new Int2FloatOpenHashMap();
		float avg = user.getAverage();
		for (ObjectValue<I> rating: user) {
			int iid = itemIndexer.getIndex(rating.getItem());
			if (iid >= similarities.length)
				continue;
			for (Map.Entry<Integer, Float> entry: similarities[iid].entrySet()) {
				int jid = entry.getKey();
				float val = entry.getValue();
				if (!user.containsObject(itemIndexer.getObject(jid))) {
					float s = 0.0f;
					float w = 0.0f;
					if (scores.containsKey(jid)) {
						s = scores.get(jid);
						w = scores.get(jid);
					}
					s += val * (rating.getRating() - avg);
					w += Math.abs(val);
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
