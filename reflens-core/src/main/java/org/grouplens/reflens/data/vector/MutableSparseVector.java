/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
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
package org.grouplens.reflens.data.vector;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Arrays;



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
	 * Construct a new zero vector with specified keys.
	 * @param keys The keys to include in the vector.
	 */
	public MutableSparseVector(LongSet keySet) {
		super(normalizeKeys(keySet), new double[keySet.size()]);
		Arrays.fill(values, 0);
	}
	
	static long[] normalizeKeys(LongSet set) {
		long[] keys = set.toLongArray();
		if (!(set instanceof LongSortedSet))
			Arrays.sort(keys);
		return keys;
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
	 * Set a value in the vector
	 * @param key The key of the value to set.
	 * @param value The value to set.
	 * @return The original value, or {@link Double#NaN} if there was no key
	 * (or if the original value was {@link Double#NaN}).
	 */
	public double set(long key, double value) {
		final int idx = Arrays.binarySearch(keys, key);
		if (idx >= 0) {
			double v = values[idx];
			values[idx] = value;
			return v;
		} else {
			return Double.NaN;
		}
	}
	
	/**
	 * Add a value to the specified entry.
	 * @param key The key whose value should be added.
	 * @param value The value to increase it by.
	 * @return The new value (or {@link Double#NaN} if no such key existed).
	 */
	public double add(long key, double value) {
		final int idx = Arrays.binarySearch(keys, key);
		if (idx >= 0) {
			return values[idx] += value;
		} else {
			return Double.NaN;
		}
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
		final int len = keys.length;
		final int olen = other.keys.length;
		int i = 0;
		int j = 0;
		while (i < len && j < olen) {
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
	 * Add another rating vector, clearing NaN values.
	 * 
	 * <p>This is like {@link #add(SparseVector)} but, if a particular value in
	 * this array is NaN and occurs in the other array, the value is replaced
	 * rather than added.
	 * @param other The vector to add.
	 */
	public void addClearNaN(final SparseVector other) {
		final int len = keys.length;
		final int olen = other.keys.length;
		int i = 0;
		int j = 0;
		while (i < len && j < olen) {
			if (keys[i] == other.keys[j]) {
				final double v = values[i];
				if (Double.isNaN(v))
					values[i] = other.values[j];
				else
					values[i] = v + other.values[j];
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
