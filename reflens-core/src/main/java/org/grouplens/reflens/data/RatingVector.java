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

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleCollections;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.grouplens.reflens.util.LongSortedArraySet;

/**
 * Representation of rating vectors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * <p>This vector class works a lot like a map, but it also caches some
 * commonly-used statistics.  The ratings are stored in parallel arrays sorted
 * by ID.  This allows fast lookup and sorted iteration.  All iterators access
 * the items in key ID.
 * 
 * <p>Rating vector support imperative mutation operations on their values, but
 * once created the set of IDs is immutable.  Addition and subtraction are
 * supported.  Mutation operations also operate in-place to reduce the
 * reallocation and copying required.  Therefore, a common pattern is:
 * 
 * <pre>
 * RatingVector normalized = vector.copy();
 * normalized.subtract(normFactor);
 * </pre>
 *
 */
public class RatingVector implements Iterable<Long2DoubleMap.Entry>, Serializable {
	private static final long serialVersionUID = 5097272716721395321L;
	private final long[] ids;
	private final double[] values;
	
	private transient Double norm;
	private transient Double sum;
	private transient Double mean;
	
	/**
	 * Construct a new vector from the contents of a map.
	 * @param ratings A map providing the values for the vector.
	 */
	public RatingVector(Long2DoubleMap ratings) {
		ids = ratings.keySet().toLongArray();
		Arrays.sort(ids);
		assert ids.length == ratings.size();
		values = new double[ids.length];
		for (int i = 0; i < ids.length; i++) {
			values[i] = ratings.get(ids[i]);
		}
	}
	
	/**
	 * Construct a new vector from existing arrays.  It is assumed that the ids
	 * are sorted and duplicate-free, and that the values is the same length.
	 * @param ids
	 * @param values
	 */
	private RatingVector(long[] ids, double[] values) {
		this.ids = ids;
		this.values = values;
	}
	
	protected void clearCachedValues() {
		norm = null;
		sum = null;
		mean = null;
	}
	
	/**
	 * Get the rating for <var>id</var>.
	 * @param id the item or user ID for which the rating is desired
	 * @return the rating (or {@link Double.NaN} if no such rating exists)
	 */
	public double get(long id) {
		return get(id, Double.NaN);
	}
	
	/**
	 * Get the rating for <var>id</var>.
	 * @param id the item or user ID for which the rating is desired
	 * @param dft The rating to return if no such rating exists
	 * @return the rating (or <var>dft</var> if no such rating exists)
	 */
	public double get(long id, double dft) {
		int idx = Arrays.binarySearch(ids, id);
		if (idx >= 0)
			return values[idx];
		else
			return dft;
	}
	
	public boolean containsId(long id) {
		return Arrays.binarySearch(ids, id) >= 0;
	}
	
	/**
	 * Iterate over all entries.
	 * @return an iterator over all ID/Rating pairs.
	 */
	@Override
	public Iterator<Long2DoubleMap.Entry> iterator() {
		return new IterImpl();
	}
	
	/**
	 * Fast iterator over all entries (it can reuse entry objects).
	 * @see Long2DoubleMap.FastEntrySet#fastIterator()
	 * @return a fast iterator over all ID/Rating pairs
	 */
	public Iterator<Long2DoubleMap.Entry> fastIterator() {
		return new FastIterImpl();
	}
	
	public Iterable<Long2DoubleMap.Entry> fast() {
		return new Iterable<Long2DoubleMap.Entry>() {
			public Iterator<Long2DoubleMap.Entry> iterator() {
				return fastIterator();
			}
		};
	}
	
	public LongSortedSet idSet() {
		return new LongSortedArraySet(ids);
	}
	
	public DoubleCollection values() {
		return DoubleCollections.unmodifiable(new DoubleArrayList(values));
	}
	
	public int size() {
		return ids.length;
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * Compute and return the L2 norm (Euclidian length) of the vector.
	 * @return The L2 norm of the vector
	 */
	public double norm() {
		if (norm == null) {
			double ssq = 0;
			for (int i = 0; i < values.length; i++) {
				double v = values[i];
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
			for (int i = 0; i < values.length; i++) {
				s += values[i];
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
			mean = ids.length > 0 ? sum() / ids.length : 0;
		}
		return mean;
	}
	
	/**
	 * Copy the rating vector.
	 * @return A new rating vector which is a copy of this one.
	 */
	public RatingVector copy() {
		double[] newvals = DoubleArrays.copy(values);
		// we can re-use the ids array since it is immutable
		return new RatingVector(ids, newvals);
	}
	
	/**
	 * Subtract another rating vector from this one.
	 * 
	 * <p>After calling this method, every element of this vector has been
	 * decreased by the corresponding element in <var>other</var>.  Elements
	 * with no corresponding element are unchanged.
	 * @param other The vector to subtract.
	 */
	public void subtract(final RatingVector other) {
		int i = 0;
		int j = 0;
		while (i < ids.length && j < other.ids.length) {
			if (ids[i] == other.ids[j]) {
				values[i] -= other.values[j];
				i++;
				j++;
			} else if (ids[i] < other.ids[j]) {
				i++;
			} else {
				j++;
			}
		}
		clearCachedValues();
	}
	
	/**
	 * Add another rating vector to this one.
	 * 
	 * <p>After calling this method, every element of this vector has been
	 * decreased by the corresponding element in <var>other</var>.  Elements
	 * with no corresponding element are unchanged.
	 * @param other The vector to add.
	 */
	public void add(final RatingVector other) {
		int i = 0;
		int j = 0;
		while (i < ids.length && j < other.ids.length) {
			if (ids[i] == other.ids[j]) {
				values[i] += other.values[j];
				i++;
				j++;
			} else if (ids[i] < other.ids[j]) {
				i++;
			} else {
				j++;
			}
		}
		clearCachedValues();
	}
	
	/**
	 * Compute the dot product of two vectors.
	 * @param other The vector to dot-product with.
	 * @return The dot product of this vector and <var>other</var>.
	 */
	public double dot(RatingVector other) {
		double dot = 0;
		int i = 0;
		int j = 0;
		while (i < ids.length && j < other.ids.length) {
			if (ids[i] == other.ids[j]) {
				dot += values[i] * other.values[j];
				i++;
				j++;
			} else if (ids[i] < other.ids[j]) {
				i++;
			} else {
				j++;
			}
		}
		return dot;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof RatingVector) {
			RatingVector vo = (RatingVector) o;
			return Arrays.equals(ids, vo.ids) && Arrays.equals(values, vo.values);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return ids.hashCode() ^ values.hashCode();
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
		return new RatingVector(vect);
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
		return new RatingVector(vect);
	}

	/**
	 * Wrap key and value arrays in a rating vector.
	 * 
	 * <p>This method allows a new rating vector to be constructed from
	 * pre-created arrays.  After wrapping arrays in a rating vector, client
	 * code should not modify them (particularly the <var>items</var> array).
	 * 
	 * @param items Array of item IDs. This array must be in sorted order and
	 * be duplicate-free.
	 * @param ratings The ratigns corresponding to the item IDs.
	 * @return A rating vector backed by the provided arrays.
	 * @throws IllegalArgumentException if there is a problem with the provided
	 * arrays (length mismatch, <var>items</var> not sorted, etc.).
	 */
	public static RatingVector wrap(long[] items, double[] ratings) {
		if (ratings.length < items.length)
			throw new IllegalArgumentException("ratings shorter than items");
		for (int i = 1; i < items.length; i++) {
			if (items[i] <= items[i-1])
				throw new IllegalArgumentException("item array not sorted");
		}
		return new RatingVector(items, ratings);
	}
	
	private final class IterImpl implements Iterator<Long2DoubleMap.Entry> {
		int pos = 0;
		@Override
		public boolean hasNext() {
			return pos < ids.length;
		}
		@Override
		public Entry next() {
			if (hasNext())
				return new Entry(pos++);
			else
				throw new NoSuchElementException();
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final class FastIterImpl implements Iterator<Long2DoubleMap.Entry> {
		Entry entry = new Entry(-1);
		@Override
		public boolean hasNext() {
			return entry.pos < ids.length - 1;
		}
		@Override
		public Entry next() {
			if (hasNext()) {
				entry.pos += 1;
				return entry;
			} else {
				throw new NoSuchElementException();
			}
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private final class Entry implements Long2DoubleMap.Entry {
		int pos;
		public Entry(int p) {
			pos = p;
		}
		@Override
		public double getDoubleValue() {
			return values[pos];
		}
		@Override
		public long getLongKey() {
			return ids[pos];
		}
		@Override
		public double setValue(double value) {
			throw new UnsupportedOperationException();
		}
		@Override
		public Long getKey() {
			return getLongKey();
		}
		@Override
		public Double getValue() {
			return getDoubleValue();
		}
		@Override
		public Double setValue(Double value) {
			return setValue(value.doubleValue());
		}
	}
}
