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
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
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
     * Invalidate the sparse vector. Any subsequent operation throws
     * {@link IllegalStateException}.
     * @see #isValid()
     */
    protected void invalidate() {
        values = null;
    }
    
    /**
     * Query whether this vector is valid.
     * 
     * <p>
     * Vectors are marked invalid by the {@link #invalidate()} method, which
     * operates by clearing the {@link #values} vector. This method is final so
     * it can be inlined aggressively.
     * 
     * @return <tt>true</tt> iff the vector is valid.
     */
    public final boolean isValid() {
        return values != null;
    }

    /**
     * Check that this vector is valid, throwing {@link IllegalStateException}
     * if it is not.
     */
    protected final void checkValid() {
        if (!isValid())
            throw new IllegalStateException("Invalid vector");
    }

    /**
     * Get the rating for <var>key</var>.
     * @param key the key to look up
     * @return the key's value (or {@link Double#NaN} if no such value exists)
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
        checkValid();
        
        int idx = Arrays.binarySearch(keys, 0, size, key);
        if (idx >= 0)
            return values[idx];
        else
            return dft;
    }

    /**
     * Query whether the vector contains an entry for the key in question. If
     * the vector contains an entry whose value is {@link Double#NaN},
     * <tt>true</tt> is still returned.
     * 
     * @param key The key to search for.
     * @return <tt>true</tt> if the key exists.
     */
    public final boolean containsKey(long key) {
        checkValid();
        return Arrays.binarySearch(keys, 0, size, key) >= 0;
    }
    
    /**
     * Deprecated alias for {@link #containsKey(long)}.
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
     * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.FastEntrySet#fastIterator()
     * 		Long2DoubleMap.FastEntrySet.fastIterator()
     * @return a fast iterator over all key/value pairs
     */
    public Iterator<Long2DoubleMap.Entry> fastIterator() {
        checkValid();
        return new FastIterImpl();
    }

    public Iterable<Long2DoubleMap.Entry> fast() {
        return new Iterable<Long2DoubleMap.Entry>() {
            @Override
            public Iterator<Long2DoubleMap.Entry> iterator() {
                return fastIterator();
            }
        };
    }

    public LongSortedSet keySet() {
        checkValid();
        return new LongSortedArraySet(keys, 0, size);
    }
    
    /**
     * Return the keys of this vector sorted by value.
     * @return A list of keys in nondecreasing order of value.
     * @see #keysByValue(boolean)
     */
    public LongArrayList keysByValue() {
        return keysByValue(false);
    }
    
    /**
     * Get the keys of this vector sorted by value.
     * @param decreasing If <var>true</var>, sort in decreasing order.
     * @return The sorted list of keys of this vector.
     */
    public LongArrayList keysByValue(boolean decreasing) {
        checkValid();
        if (!isComplete())
            throw new IllegalStateException();
        long[] skeys = Arrays.copyOf(keys, size);
        
        LongComparator cmp;
        // Set up the comparator. We use the key as a secondary comparison to get
        // a reproducible sort irrespective of sorting algorithm.
        if (decreasing) {
            cmp = new AbstractLongComparator() {
                @Override
                public int compare(long k1, long k2) {
                    int c = Double.compare(get(k2), get(k1));
                    if (c != 0)
                        return c;
                    else
                        return Longs.compare(k1, k2);
                }
            };
        } else {
            cmp = new AbstractLongComparator() {
                @Override
                public int compare(long k1, long k2) {
                    int c = Double.compare(get(k1), get(k2));
                    if (c != 0)
                        return c;
                    else
                        return Longs.compare(k1, k2);
                }
            };
        }
        
        LongArrays.quickSort(skeys, cmp);
        return LongArrayList.wrap(skeys);
    }

    public DoubleCollection values() {
        checkValid();
        return DoubleCollections.unmodifiable(new DoubleArrayList(values, 0, size));
    }

    public final int size() {
        checkValid();
        return size;
    }

    public final boolean isEmpty() {
        checkValid();
        return size == 0;
    }

    /**
     * Compute and return the L2 norm (Euclidian length) of the vector.
     * @return The L2 norm of the vector
     */
    public double norm() {
        checkValid();
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
        checkValid();
        if (sum == null) {
            double s = 0;
            for (int i = 0; i < size; i++) {
            	if (!Double.isNaN(values[i]))
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
        checkValid();
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
        checkValid();
        other.checkValid();
        double dot = 0;
        int i = 0;
        int j = 0;
        final int sz1 = size;
        final int sz2 = other.size;
        while (i < sz1 && j < sz2) {
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
    
    /**
     * Count the common keys of two vectors.
     * @param other The vector to count mutual keys with.
     * @return The number of keys shared between this vector and <var>other</var>.
     */
    public int countCommonKeys(SparseVector other) {
        checkValid();
        other.checkValid();
        int n = 0;
        int i = 0;
        int j = 0;
        final int sz1 = size;
        final int sz2 = other.size;
        while (i < sz1 && j < sz2) {
            if (keys[i] == other.keys[j]) {
                n++;
                i++;
                j++;
            } else if (keys[i] < other.keys[j]) {
                i++;
            } else {
                j++;
            }
        }
        return n;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof SparseVector) {
            SparseVector vo = (SparseVector) o;
            if (!isValid()) return false;
            if (!vo.isValid()) return false;
            
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
        if (!isValid()) return 0;
        if (hashCode == null) {
            int hash = 0;
            final int sz = size();
            for (int i = 0; i < sz; i++) {
                hash ^= Longs.hashCode(keys[i]);
                hash ^= Doubles.hashCode(values[i]);
            }
            hashCode = hash;
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
        checkValid();
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
    
    public boolean isComplete() {
        if (!isValid()) return false;
        for (int i = 0; i < size; i++) {
    		if (Double.isNaN(values[i])) return false;
    	}
    	return true;
    }

    final class IterImpl implements Iterator<Long2DoubleMap.Entry> {
        int pos = 0;
        @Override
        public boolean hasNext() {
            return pos < size;
        }
        @Override
        public Entry next() {
            if (!isValid()) throw new ConcurrentModificationException();
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
            if (!isValid()) throw new ConcurrentModificationException();
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
        checkValid();
        return new ImmutableSparseVector(keys, values);
    }
    
    /**
     * Return a mutable copy of this sparse vector.
     * @return A mutable sparse vector which can be modified without modifying
     * this vector.
     */
    public MutableSparseVector mutableCopy() {
        checkValid();
        return new MutableSparseVector(keys, Arrays.copyOf(values, values.length));
    }

    /**
     * @deprecated Use {@link MutableSparseVector#wrap(long[], double[])}
     */
    @Deprecated
    public static MutableSparseVector wrap(long[] keys, double[] values) {
        return MutableSparseVector.wrap(keys, values);
    }
    
    /**
     * @deprecated Use {@link MutableSparseVector#wrap(long[], double[])}
     */
    @Deprecated
    public static MutableSparseVector wrap(long[] keys, double[] values, int length) {
        return MutableSparseVector.wrap(keys, values, length);
    }

    /**
     * @deprecated Use {@link MutableSparseVector#wrap(long[], double[], boolean)}
     */
    @Deprecated
    public static MutableSparseVector wrap(long[] keys, double[] values, boolean removeNaN) {
        return MutableSparseVector.wrap(keys, values, removeNaN);
    }
}
