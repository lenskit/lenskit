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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;

/**
 * Read-only interface to sparse vectors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * <p>This vector class works a lot like a map, but it also caches some
 * commonly-used statistics.  The values are stored in parallel arrays sorted
 * by ID.  This allows fast lookup and sorted iteration.  All iterators access
 * the items in key ID.
 *
 * <p>It is possible for vectors to contain NaN values, but be careful with this.
 * They will show up in enumeration and {@link #containsKey(long)} will return
 * <tt>true</tt>, but {@link #get(long)} will not distinguish between them and
 * missing entries ({@link #get(long, double)} will).
 * 
 * <p>This class provides a <em>read-only</em> interface to sparse vectors. It
 * may actually be a {@link MutableSparseVector}, so the data may be modified
 * by code elsewhere that has access to the mutable representation. For sparse
 * vectors that are guaranteed to be unchanging, see {@link ImmutableSparseVector}.
 *
 */
public abstract class SparseVector implements Iterable<Long2DoubleMap.Entry>, Serializable, Cloneable {
    private static final long serialVersionUID = 5097272716721395321L;
    protected final long[] keys;
    protected double[] values;
    protected final int size;
    private volatile transient Double norm;
    private volatile transient Double sum;
    private volatile transient Double mean;
    private volatile transient Integer hashCode;

    protected SparseVector(Long2DoubleMap ratings) {
        keys = ratings.keySet().toLongArray();
        size = keys.length;
        Arrays.sort(keys);
        assert keys.length == ratings.size();
        assert isSorted(keys, size);
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
        size = keys.length;
        assert isSorted(keys, size);
    }
    
    /**
     * Construct a new sparse vector from existing arrays.  The arrays must
     * be sorted by key; no checking is done.
     * @param keys The array of keys.
     * @param values The keys' values.
     * @param length The size of the vector. Only the first <var>length</var>
     * elements are used. 
     */
    protected SparseVector(long[] keys, double[] values, int length) {
        this.keys = keys;
        this.values = values;
        size = length;
        assert isSorted(keys, length);
    }

    public static boolean isSorted(long[] array, int length) {
        for (int i = 1; i < length; i++) {
            if (array[i] <= array[i-1])
                return false;
        }
        return true;
    }

    protected void clearCachedValues() {
        norm = null;
        sum = null;
        mean = null;
        hashCode = null;
    }

    /**
     * Get the rating for <var>key</var>.
     * @param key the key to look up
     * @return the key's value (or {@link Double.NaN} if no such value exists)
     */
    public final double get(long key) {
        return get(key, Double.NaN);
    }

    /**
     * Get the rating for <var>key</var>.
     * @param key the key to look up
     * @param dft The value to return if the key is not in the vector
     * @return the value (or <var>dft</var> if no such key exists)
     */
    public final double get(long key, double dft) {
        int idx = Arrays.binarySearch(keys, 0, size, key);
        if (idx >= 0)
            return values[idx];
        else
            return dft;
    }

    /**
     * Query whether the vector contains an entry for the key in question.  If
     * the vector contains an entry whose value is {@link Double#NaN}, <tt>true</tt>
     * is still returned.  This can change if it is not useful. 
     * @param key The key to search for.
     * @return <tt>true</tt> if the key exists.
     */
    public final boolean containsKey(long key) {
        return Arrays.binarySearch(keys, 0, size, key) >= 0;
    }
    
    /**
     * Deprecated alias for {@link #containsKey(long)}.
     * @param id
     * @return
     */
    @Deprecated
    public final boolean containsId(long id) {
        return containsKey(id);
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
        return new LongSortedArraySet(keys, 0, size);
    }

    public DoubleCollection values() {
        return DoubleCollections.unmodifiable(new DoubleArrayList(values, 0, size));
    }

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    /**
     * Compute and return the L2 norm (Euclidian length) of the vector.
     * @return The L2 norm of the vector
     */
    public double norm() {
        if (norm == null) {
            double ssq = 0;
            for (int i = 0; i < size; i++) {
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
            for (int i = 0; i < size; i++) {
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
            mean = size > 0 ? sum() / size : 0;
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
        while (i < size && j < other.size) {
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
            int sz = size();
            int osz = vo.size();
            if (sz != osz) {
                return false;
            } else {
                final long[] ks = keys;
                final long[] kso = vo.keys;
                final double[] vs = values;
                final double[] vso = vo.values;
                for (int i = 0; i < sz; i++) {
                    if (ks[i] != kso[i])
                        return false;
                    if (vs[i] != vso[i])
                        return false;
                }
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            int hash = 0;
            final int sz = size();
            for (int i = 0; i < sz; i++) {
                hash ^= Longs.hashCode(keys[i]);
                hash ^= Doubles.hashCode(values[i]);
            }
        }
        return hashCode;
    }

    /**
     * Clone the sparse vector. This implementation duplicates the value array
     * so that the new sparse vector is disconnected; immutable implementations
     * may leave them connected.
     */
    @Override
    public SparseVector clone() {
        return clone(true);
    }
    
    /**
     * Clone method parameterized on whether to copy the value array.
     * @param copyValues If <tt>true</tt>, make a copy of the value array in the
     * new object.
     * @return A clone of this sparse vector.
     */
    protected SparseVector clone(boolean copyValues) {
        SparseVector v;
        try {
            v = (SparseVector) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (copyValues)
            v.values = DoubleArrays.copy(v.values);
        return v;
    }

    final class IterImpl implements Iterator<Long2DoubleMap.Entry> {
        int pos = 0;
        @Override
        public boolean hasNext() {
            return pos < size;
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
            return entry.pos < size - 1;
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
     * Return an immutable snapshot of this sparse vector.
     * @return An immutable sparse vector whose contents are the same as this
     * vector. If the vector is already immutable, the returned object may be
     * identical.
     */
    public ImmutableSparseVector immutable() {
        return new ImmutableSparseVector(keys, values);
    }
    
    /**
     * Return a mutable copy of this sparse vector.
     * @return A mutable sparse vector which can be modified without modifying
     * this vector.
     */
    public MutableSparseVector mutableCopy() {
        return new MutableSparseVector(keys, Arrays.copyOf(values, values.length));
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
    public static MutableSparseVector wrap(long[] keys, double[] values) {
        if (values.length < keys.length)
            throw new IllegalArgumentException("ratings shorter than items");
        if (!isSorted(keys, keys.length))
            throw new IllegalArgumentException("item array not sorted");
        return new MutableSparseVector(keys, values);
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
    public static MutableSparseVector wrap(long[] keys, double[] values, boolean removeNaN) {
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
        if (!isSorted(keys, keys.length))
            throw new IllegalArgumentException("key array not sorted");
        return wrap(keys, values);
    }
}
