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

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Collection;


/**
 * Mutable sparse vector interface
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * <p>This extends the sparse vector with support for imperative mutation
 * operations on their values, but
 * once created the set of keys remains immutable.  Addition and subtraction are
 * supported.  Mutation operations also operate in-place to reduce the
 * reallocation and copying required.  Therefore, a common pattern is:
 * 
 * <pre>
 * MutableSparseVector normalized = MutableSparseVector.copy(vector);
 * normalized.subtract(normFactor);
 * </pre>
 *
 */
public class MutableSparseVector extends SparseVector {
	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new vector from the contents of a map.
	 * @param ratings A map providing the values for the vector.
	 */
	public MutableSparseVector(Long2DoubleMap ratings) {
		super(ratings);
	}
	
	/**
	 * Construct a new vector from existing arrays.  It is assumed that the keys
	 * are sorted and duplicate-free, and that the values is the same length.
	 * @param keys
	 * @param values
	 */
	private MutableSparseVector(long[] keys, double[] values) {
		super(keys, values);
	}
	
	/**
	 * Subtract another rating vector from this one.
	 * 
	 * <p>After calling this method, every element of this vector has been
	 * decreased by the corresponding element in <var>other</var>.  Elements
	 * with no corresponding element are unchanged.
	 * @param other The vector to subtract.
	 */
	public void subtract(final SparseVector other) {
		int i = 0;
		int j = 0;
		while (i < keys.length && j < other.keys.length) {
			if (keys[i] == other.keys[j]) {
				values[i] -= other.values[j];
				i++;
				j++;
			} else if (keys[i] < other.keys[j]) {
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
	public void add(final SparseVector other) {
		int i = 0;
		int j = 0;
		while (i < keys.length && j < other.keys.length) {
			if (keys[i] == other.keys[j]) {
				values[i] += other.values[j];
				i++;
				j++;
			} else if (keys[i] < other.keys[j]) {
				i++;
			} else {
				j++;
			}
		}
		clearCachedValues();
	}
	
	/**
	 * Copy the rating vector.
	 * @return A new rating vector which is a copy of this one.
	 */
	public MutableSparseVector copy() {
		double[] newvals = DoubleArrays.copy(values);
		// we can re-use the keys array since it is immutable
		return new MutableSparseVector(keys, newvals);
	}
	
	/**
	 * Create a mutable copy of a sparse vector.
	 * @param vector The base vector.
	 * @return A mutable copy of <var>vector</var>.
	 */
	public static MutableSparseVector copy(SparseVector vector) {
		return new MutableSparseVector(vector.keys, DoubleArrays.copy(vector.values));
	}
	
	@Override
	public MutableSparseVector clone() {
		return (MutableSparseVector) super.clone();
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
	 * Wrap key and value arrays in a mutable sparse vector.
	 * @see SparseVector#wrap(long[], double[])
	 */
	public static MutableSparseVector wrap(long[] keys, double[] values) {
		if (values.length < keys.length)
			throw new IllegalArgumentException("ratings shorter than items");
		for (int i = 1; i < keys.length; i++) {
			if (keys[i] <= keys[i-1])
				throw new IllegalArgumentException("item array not sorted");
		}
		return new MutableSparseVector(keys, values);
	}
}
