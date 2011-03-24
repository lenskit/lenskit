/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.data.vector;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleCollections;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.grouplens.lenskit.util.LongSortedArraySet;

/**
 * Sparse vector representation
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * <p>This vector class works a lot like a map, but it also caches some
 * commonly-used statistics.  The values are stored in parallel arrays sorted
 * by ID.  This allows fast lookup and sorted iteration.  All iterators access
 * the items in key ID.
 *
 * <p>It is possible for vectors to contain NaN values, but be careful with this.
 * They will show up in enumeration and {@link #containsId(long)} will return
 * <tt>true</tt>, but {@link #get(long)} will not distinguish between them and
 * missing entries.
 *
 */
public class SparseVector implements Iterable<Long2DoubleMap.Entry>, Serializable, Cloneable {
    private static final long serialVersionUID = 5097272716721395321L;
    protected final long[] keys;
    protected double[] values;
    private transient Double norm;
    private transient Double sum;
    private transient Double mean;

    public SparseVector(Long2DoubleMap ratings) {
        keys = ratings.keySet().toLongArray();
        Arrays.sort(keys);
        assert keys.length == ratings.size();
        assert isSorted(keys);
        values = new double[keys.length];
        final int len = keys.length;
        for (int i = 0; i < len; i++) {
            values[i] = ratings.get(keys[i]);
        }
    }

    /**
     * Construct a new sparse vector from existing arrays.  The arrays must
     * be sorted by key; no checking is done.
     * @param keys The array of keys.
     * @param values The keys' values.
     */
    protected SparseVector(long[] keys, double[] values) {
        this.keys = keys;
        this.values = values;
        assert isSorted(keys);
    }

    public static boolean isSorted(long[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] <= array[i-1])
                return false;
        }
        return true;
    }

    protected void clearCachedValues() {
        norm = null;
        sum = null;
        mean = null;
    }

    /**
     * Get the rating for <var>key</var>.
     * @param key the key to look up
     * @return the key's value (or {@link Double.NaN} if no such value exists)
     */
    public double get(long key) {
        return get(key, Double.NaN);
    }

    /**
     * Get the rating for <var>key</var>.
     * @param key the key to look up
     * @param dft The value to return if the key is not in the vector
     * @return the value (or <var>dft</var> if no such key exists)
     */
    public double get(long key, double dft) {
        int idx = Arrays.binarySearch(keys, key);
        if (idx >= 0)
            return values[idx];
        else
            return dft;
    }

    public boolean containsId(long id) {
        return Arrays.binarySearch(keys, id) >= 0;
    }

    /**
     * Iterate over all entries.
     * @return an iterator over all key/value pairs.
     */
    @Override
    public Iterator<Long2DoubleMap.Entry> iterator() {
        return new IterImpl();
    }

    /**
     * Fast iterator over all entries (it can reuse entry objects).
     * @see Long2DoubleMap.FastEntrySet#fastIterator()
     * @return a fast iterator over all key/value pairs
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

    public LongSortedSet keySet() {
        return new LongSortedArraySet(keys);
    }

    public DoubleCollection values() {
        return DoubleCollections.unmodifiable(new DoubleArrayList(values));
    }

    public int size() {
        return keys.length;
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
            mean = keys.length > 0 ? sum() / keys.length : 0;
        }
        return mean;
    }

    /**
     * Compute the dot product of two vectors.
     * @param other The vector to dot-product with.
     * @return The dot product of this vector and <var>other</var>.
     */
    public double dot(SparseVector other) {
        double dot = 0;
        int i = 0;
        int j = 0;
        while (i < keys.length && j < other.keys.length) {
            if (keys[i] == other.keys[j]) {
                dot += values[i] * other.values[j];
                i++;
                j++;
            } else if (keys[i] < other.keys[j]) {
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
        } else if (o instanceof SparseVector) {
            SparseVector vo = (SparseVector) o;
            return Arrays.equals(keys, vo.keys) && Arrays.equals(values, vo.values);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return keys.hashCode() ^ values.hashCode();
    }

    @Override
    public SparseVector clone() {
        SparseVector v;
        try {
            v = (SparseVector) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        v.values = DoubleArrays.copy(v.values);
        return v;
    }

    final class IterImpl implements Iterator<Long2DoubleMap.Entry> {
        int pos = 0;
        @Override
        public boolean hasNext() {
            return pos < keys.length;
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

    final class FastIterImpl implements Iterator<Long2DoubleMap.Entry> {
        Entry entry = new Entry(-1);
        @Override
        public boolean hasNext() {
            return entry.pos < keys.length - 1;
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
            return keys[pos];
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

    /**
     * Wrap key and value arrays in a sparse vector.
     *
     * <p>This method allows a new vector to be constructed from
     * pre-created arrays.  After wrapping arrays in a rating vector, client
     * code should not modify them (particularly the <var>items</var> array).
     *
     * @param keys Array of entry keys. This array must be in sorted order and
     * be duplicate-free.
     * @param values The values for the vector.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     * arrays (length mismatch, <var>keys</var> not sorted, etc.).
     */
    public static SparseVector wrap(long[] keys, double[] values) {
        if (values.length < keys.length)
            throw new IllegalArgumentException("ratings shorter than items");
        if (!isSorted(keys))
            throw new IllegalArgumentException("item array not sorted");
        return new SparseVector(keys, values);
    }

    /**
     * Wrap key and value arrays in a sparse vector.
     *
     * <p>This method allows a new vector to be constructed from
     * pre-created arrays.  After wrapping arrays in a rating vector, client
     * code should not modify them (particularly the <var>items</var> array).
     *
     * <p>The arrays may be modified, particularly to remove NaN values.  The
     * client should not depend on them exhibiting any particular behavior aftor
     * calling this method.
     *
     * @param keys Array of entry keys. This array must be in sorted order and
     * be duplicate-free.
     * @param values The values for the vector.
     * @param values If true, remove NaN values from the arrays.
     * @return A sparse vector backed by the provided arrays.
     * @throws IllegalArgumentException if there is a problem with the provided
     * arrays (length mismatch, <var>keys</var> not sorted, etc.).
     */
    public static SparseVector wrap(long[] keys, double[] values, boolean removeNaN) {
        if (removeNaN) {
            int pos = 0;
            for (int i = 0; i < keys.length; i++) {
                if (!Double.isNaN(values[i])) {
                    if (i != pos) {
                        keys[pos] = keys[i];
                        values[pos] = values[i];
                    }
                    pos++;
                }
            }
            if (pos < keys.length) {
                keys = LongArrays.copy(keys, 0, pos);
                values = DoubleArrays.copy(values, 0, pos);
            }
        }
        if (values.length < keys.length)
            throw new IllegalArgumentException("key/value length mismatch");
        if (!isSorted(keys))
            throw new IllegalArgumentException("key array not sorted");
        return wrap(keys, values);
    }
}
