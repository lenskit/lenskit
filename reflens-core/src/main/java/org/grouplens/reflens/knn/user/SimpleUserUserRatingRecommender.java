package org.grouplens.reflens.knn.user;

import static java.lang.Math.abs;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import javax.annotation.WillNotClose;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.knn.Similarity;
import org.grouplens.reflens.knn.params.NeighborhoodSize;
import org.grouplens.reflens.knn.params.UserSimilarity;
import org.grouplens.reflens.util.CollectionUtils;
import org.grouplens.reflens.util.LongSortedArraySet;

import com.google.inject.Inject;

/**
 * Actual predictor and recommender using the raw search.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleUserUserRatingRecommender implements RatingRecommender,
		RatingPredictor {
	
	/**
	 * Representation of neighbors.
	 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
	 *
	 */
	static class Neighbor {
		public final long userId;
		public final SparseVector ratings;
		public final double similarity;
		public Neighbor(long user, SparseVector rv, double sim) {
			userId = user;
			ratings = rv;
			similarity = sim;
		}
	}
	
	/**
	 * Compartor to order neighbors by similarity.
	 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
	 *
	 */
	static class NeighborSimComparator implements Comparator<Neighbor> {
		public int compare(Neighbor n1, Neighbor n2) {
			return Double.compare(n1.similarity, n2.similarity);
		}
	}
	
	private final RatingDataSource dataSource;
	private final int neighborhoodSize;
	private final Similarity<? super SparseVector> similarity;

	/**
	 * Construct a new user-user recommender.
	 * @param data The data source to scan.
	 * @param nnbrs The number of neighbors to consider for each item.
	 * @param similarity The similarity function to use.
	 */
	@Inject
	SimpleUserUserRatingRecommender(@WillNotClose RatingDataSource data,
			@NeighborhoodSize int nnbrs,
			@UserSimilarity Similarity<? super SparseVector> similarity) {
		dataSource = data;
		neighborhoodSize = nnbrs;
		this.similarity = similarity;
	}
	
	/**
	 * Find the neighbors for a user with respect to a collection of items.
	 * For each item, the <var>neighborhoodSize</var> users closest to the
	 * provided user are returned.
	 * 
	 * @param uid The user ID.
	 * @param ratings The user's ratings vector.
	 * @param items The items for which neighborhoods are requested.
	 * @return A mapping of item IDs to neighborhoods.
	 */
	protected Long2ObjectMap<? extends Collection<Neighbor>> findNeighbors(long uid, SparseVector ratings, LongSet items) {
		Long2ObjectMap<PriorityQueue<Neighbor>> heaps = initNeighborHeaps(items);
		
		Cursor<UserRatingProfile> users = dataSource.getUserRatingProfiles();
		
		try {
			for (UserRatingProfile user: users) {
				if (user.getUser() == uid) continue;
				
				final SparseVector urv = user.getRatingVector(); 
				final double sim = similarity.similarity(ratings, urv);
				final Neighbor n = new Neighbor(user.getUser(), urv, sim);
				
				LongIterator iit = items.iterator();
				while (iit.hasNext()) {
					final long item = iit.nextLong();
					if (urv.containsId(item)) {
						PriorityQueue<Neighbor> heap = heaps.get(item);
						heap.add(n);
						if (heap.size() > neighborhoodSize) {
							assert heap.size() == neighborhoodSize + 1;
							heap.remove();
						}
					}
				}
			}
		} finally {
			users.close();
		}
		return heaps;
	}

	private Long2ObjectMap<PriorityQueue<Neighbor>> initNeighborHeaps(
			LongSet items) {
		Long2ObjectMap<PriorityQueue<Neighbor>> heaps =
			new Long2ObjectOpenHashMap<PriorityQueue<Neighbor>>(items.size());
		final Comparator<Neighbor> comp = new NeighborSimComparator();
		LongIterator iter = items.iterator();
		while (iter.hasNext()) {
			final long item = iter.nextLong();
			heaps.put(item, new PriorityQueue<Neighbor>(neighborhoodSize + 1, comp));
		}
		return heaps;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, org.grouplens.reflens.data.vector.SparseVector, long)
	 */
	@Override
	public ScoredId predict(long user, SparseVector ratings, long item) {
		LongSet items = LongSets.singleton(item);
		SparseVector vector = predict(user, ratings, items);
		if (vector.containsId(item))
			return new ScoredId(user, vector.get(item));
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingPredictor#predict(long, org.grouplens.reflens.data.vector.SparseVector, java.util.Collection)
	 */
	@Override
	public SparseVector predict(long user, SparseVector ratings,
			Collection<Long> items) {
		Long2ObjectMap<? extends Collection<Neighbor>> neighborhoods =
			findNeighbors(user, ratings, new LongSortedArraySet(items));
		long[] keys = CollectionUtils.fastCollection(items).toLongArray();
		if (!(items instanceof LongSortedSet))
			Arrays.sort(keys);
		double[] preds = new double[keys.length];
		for (int i = 0; i < keys.length; i++) {
			final long item = keys[i];
			double sum = 0;
			double weight = 0;
			for (final Neighbor n: neighborhoods.get(item)) {
				weight += abs(n.similarity);
				sum += n.similarity * n.ratings.get(item);
			}
			preds[i] = sum / weight;
		}
		return SparseVector.wrap(keys, preds, true);
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RatingRecommender#recommend(long, org.grouplens.reflens.data.vector.SparseVector)
	 */
	@Override
	public List<ScoredId> recommend(long user, SparseVector ratings) {
		// TODO Auto-generated method stub
		return null;
	}

}
