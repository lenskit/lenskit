package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Collection;

import org.grouplens.lenskit.data.vector.MutableSparseVector;

/**
 * Utilities for working with ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class Ratings {

	/** 
	 * Construct a rating vector that contains the ratings provided by each user.
	 * If all ratings in <var>ratings</var> are for the same item, then this
	 * will be a valid item rating vector.  If multiple ratings are by the same
	 * user, the one with the highest timestamp is retained.  If two ratings
	 * by the same user have identical timestamps, then the one that occurs last
	 * when the collection is iterated is retained.
	 * 
	 * @param ratings Some ratings (they should all be for the same item)
	 * @return A sparse vector mapping user IDs to ratings.
	 */
	public static MutableSparseVector itemRatingVector(Collection<Rating> ratings) {
		Long2DoubleMap vect = new Long2DoubleOpenHashMap();
		Long2LongMap tsMap = new Long2LongOpenHashMap();
		tsMap.defaultReturnValue(Long.MIN_VALUE);
		for (Rating r: ratings) {
			long uid = r.getUserId();
			long ts = r.getTimestamp();
			if (ts >= tsMap.get(uid)) {
				vect.put(uid, r.getRating());
				tsMap.put(uid, ts);
			}
		}
		return new MutableSparseVector(vect);
	}

	/**
	 * Construct a rating vector that contains the ratings provided for each item.
	 * If all ratings in <var>ratings</var> are by the same user, then this will
	 * be a valid user rating vector.  If multiple ratings are provided for the
	 * same item, the one with the greatest timestamp is retained.  Ties are
	 * broken by preferring ratings which come later when iterating through the
	 * collection.
	 * 
	 * @param ratings A collection of ratings (should all be by the same user)
	 * @return A sparse vector mapping item IDs to ratings
	 */
	public static MutableSparseVector userRatingVector(Collection<Rating> ratings) {
		Long2DoubleMap vect = new Long2DoubleOpenHashMap();
		Long2LongMap tsMap = new Long2LongOpenHashMap();
		tsMap.defaultReturnValue(Long.MIN_VALUE);
		for (Rating r: ratings) {
			long iid = r.getItemId();
			long ts = r.getTimestamp();
			if (ts >= tsMap.get(iid)) {
				vect.put(r.getItemId(), r.getRating());
				tsMap.put(iid, ts);
			}
		}
		return new MutableSparseVector(vect);
	}

}
