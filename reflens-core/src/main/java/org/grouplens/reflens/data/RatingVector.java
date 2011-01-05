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

package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Collection;
import java.util.Iterator;

/**
 * Representation of rating vectors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This vector class works a lot like a map, but it also caches some
 * commonly-used statistics.
 *
 */
public class RatingVector implements Iterable<Long2DoubleMap.Entry> {
	private final Long2DoubleOpenHashMap ratings;
	private Double norm;
	private Double sum;
	private Double mean;
	
	public RatingVector() {
		ratings = new Long2DoubleOpenHashMap();
		ratings.defaultReturnValue(Double.NaN);
	}
	
	public RatingVector(int size) {
		ratings = new Long2DoubleOpenHashMap(size);
		ratings.defaultReturnValue(Double.NaN);
	}
	
	protected void clearCachedValues() {
		norm = null;
		sum = null;
		mean = null;
	}
	
	/**
	 * Put a rating for <var>id</var>.  If a more recent rating for <var>id</var>
	 * exists, this rating is discarded.
	 * @param id the user or item ID
	 * @param rating the rating
	 */
	public void put(long id, double rating) {
		clearCachedValues();
		ratings.put(id, rating);
	}
	
	/**
	 * Get the rating for <var>id</var>.
	 * @param id the item or user ID for which the rating is desired
	 * @return the rating (or {@link Double.NaN} if no such rating exists)
	 */
	public double get(long id) {
		return ratings.get(id);
	}
	
	public boolean containsId(long id) {
		return ratings.containsKey(id);
	}
	
	/**
	 * Iterate over all entries.
	 * @return an iterator over all ID/Rating pairs.
	 */
	@Override
	public Iterator<Long2DoubleMap.Entry> iterator() {
		return ratings.long2DoubleEntrySet().iterator();
	}
	
	/**
	 * Fast iterator over all entries (it can reuse entry objects).
	 * @see Long2DoubleMap.FastEntrySet#fastIterator()
	 * @return a fast iterator over all ID/Rating pairs
	 */
	public Iterator<Long2DoubleMap.Entry> fastIterator() {
		return ratings.long2DoubleEntrySet().fastIterator();
	}
	
	public Iterable<Long2DoubleMap.Entry> fast() {
		return new Iterable<Long2DoubleMap.Entry>() {
			public Iterator<Long2DoubleMap.Entry> iterator() {
				return fastIterator();
			}
		};
	}
	
	public LongSet idSet() {
		return ratings.keySet();
	}
	
	public DoubleCollection values() {
		return ratings.values();
	}
	
	public int size() {
		return ratings.size();
	}
	
	public boolean isEmpty() {
		return ratings.isEmpty();
	}
	
	/**
	 * Compute and return the L2 norm (Euclidian length) of the vector.
	 * @return The L2 norm of the vector
	 */
	public double norm() {
		if (norm == null) {
			double ssq = 0;
			DoubleIterator iter = values().iterator();
			while (iter.hasNext()) {
				double v = iter.nextDouble();
				ssq += v * v;
			}
			norm = Math.sqrt(ssq);
		}
		return norm;
	}
	
	/**
	 * Compute and return the L1 norm (sum) of the vector
	 * @return the sum of the vector's values
	 */
	public double sum() {
		if (sum == null) {
			double s = 0;
			DoubleIterator iter = values().iterator();
			while (iter.hasNext()) {
				s += iter.nextDouble();
			}
			sum = s;
		}
		return sum;
	}
	
	/**
	 * Compute and return the mean of the vector's values
	 * @return the mean of the vector
	 */
	public double mean() {
		if (mean == null) {
			mean = sum() / size();
		}
		return mean;
	}
	
	/**
	 * Construct a rating vector that contains the ratings provided by each item.
	 * If all ratings in <var>ratings</var> are by the same user, then this will
	 * be a valid user rating vector.  If multiple ratings are provided for the
	 * same item, the one with the greatest timestamp is retained.  Ties are
	 * broken by preferring ratings which come later when iterating through the
	 * collection.
	 * 
	 * @param ratings A collection of ratings (should all be by the same user)
	 * @return A rating vector mapping item IDs to ratings
	 */
	public static RatingVector userRatingVector(Collection<Rating> ratings) {
		RatingVector v = new RatingVector(ratings.size());
		Long2LongMap tsMap = new Long2LongOpenHashMap();
		tsMap.defaultReturnValue(Long.MIN_VALUE);
		for (Rating r: ratings) {
			long iid = r.getItemId();
			long ts = r.getTimestamp();
			if (ts >= tsMap.get(iid)) {
				v.put(r.getItemId(), r.getRating());
				tsMap.put(iid, ts);
			}
		}
		return v;
	}
	
	/** 
	 * Construct a rating vector that contains the ratings provided by each user.
	 * If all ratings in <var>ratings</var> are for the same item, then this
	 * will be a valid item rating vector.  If multiple ratings are by the same
	 * user, the one with the highest timestamp is retained.  If two ratings
	 * by the same user have identical timestamps, then the one that occurs last
	 * when the collection is iterated is retained.
	 * 
	 * @param ratings Some ratings (they should all be for the same item)
	 * @return A rating vector mapping user IDs to ratings.
	 */
	public static RatingVector itemRatingVector(Collection<Rating> ratings) {
		RatingVector v = new RatingVector(ratings.size());
		Long2LongMap tsMap = new Long2LongOpenHashMap();
		tsMap.defaultReturnValue(Long.MIN_VALUE);
		for (Rating r: ratings) {
			long uid = r.getUserId();
			long ts = r.getTimestamp();
			if (ts >= tsMap.get(uid)) {
				v.put(uid, r.getRating());
				tsMap.put(uid, ts);
			}
		}
		return v;
	}
}
