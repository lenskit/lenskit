/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.MoreArrays;

/**
 * Immutable sparse vectors. These vectors cannot be changed, even by other
 * code, and are therefore safe to store and are thread-safe.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@Immutable
public class ImmutableSparseVector extends SparseVector implements Serializable {
    private static final long serialVersionUID = -4740588973577998934L;
    
    protected final long[] keys;
    protected double[] values;
    protected final int size;

    /**
     * Create a new, empty immutable sparse vector.
     */
    public ImmutableSparseVector() {
        keys = new long[0];
        values = null;
        size = 0;
    }

    /**
     * Create a new immutable sparse vector from a map of ratings.
     * 
     * @param ratings The ratings to make a vector from. Its key set is used as
     *        the vector's key domain.
     */
    public ImmutableSparseVector(Long2DoubleMap ratings) {
        keys = ratings.keySet().toLongArray();
        size = keys.length;
        Arrays.sort(keys);
        assert keys.length == ratings.size();
        assert MoreArrays.isSorted(keys, 0, size);
        values = new double[keys.length];
        final int len = keys.length;
        for (int i = 0; i < len; i++) {
            values[i] = ratings.get(keys[i]);
        }
    }
    
    /**
     * Construct a new sparse vector from pre-existing arrays. These arrays must
     * be sorted in key order and cannot contain duplicate keys; this condition
     * is not checked.
     * 
     * @param keys The key array (will be the key domain).
     * @param values The value array.
     * @param size The length to actually use.
     */
    protected ImmutableSparseVector(long[] keys, double[] values, int size) {    
        this.keys = keys;
        this.values = values;
        this.size = size;
        assert MoreArrays.isSorted(keys, 0, size);
    }
    
    @Override
    public final double get(long key, double dft) {
        int idx = Arrays.binarySearch(keys, 0, size, key);
        if (idx >= 0)
            return values[idx];
        else
            return dft;
    }
    
    @Override
    public final boolean containsKey(long key) {
        return Arrays.binarySearch(keys, 0, size, key) >= 0;
    }
    
    @Override
    public Iterator<Long2DoubleMap.Entry> iterator() {
        return new IterImpl();
    }
    
    @Override
    public Iterator<Long2DoubleMap.Entry> fastIterator() {
        return new FastIterImpl();
    }
    
    @Override
    public LongSortedSet keySet() {
        return LongSortedArraySet.wrap(keys, size);
    }
    
    @Override
    public LongSortedSet keyDomain() {
        return keySet();
    }
    
    @Override
    public DoubleList values() {
        return DoubleLists.unmodifiable(new DoubleArrayList(values, 0, size));
    }
    
    @Override
    public int size() {
        return size;
    }
    
    /**
     * Reimplement {@link SparseVector#dot(SparseVector)} to use an optimized
     * implementation when computing the dot product of two immutable sparse
     * vectors.
     * @see SparseVector#dot(SparseVector)
     */
    @Override
    public double dot(SparseVector o) {
        if (o instanceof ImmutableSparseVector) {
            // we can speed this up a lot
            ImmutableSparseVector iv = (ImmutableSparseVector) o;
            double dot = 0;
            
            final int sz = size;
            final int osz = iv.size;
            int i = 0, j = 0;
            while (i < sz && j < osz) {
                final long k1 = keys[i];
                final long k2 = iv.keys[j];
                if (k1 == k2) {
                    dot += values[i] * iv.values[j];
                    i++;
                    j++;
                } else if (k1 < k2) {
                    i++;
                } else {
                    j++;
                }
            }
            
            return dot;
        } else {
            return super.dot(o);
        }
    }
    
    /**
     * Reimplement {@link SparseVector#countCommonKeys(SparseVector)} to be more
     * efficient when computing common keys of two immutable sparse vectors.
     * @see SparseVector#countCommonKeys(SparseVector)
     */
    @Override
    public int countCommonKeys(SparseVector o) {
        if (o instanceof ImmutableSparseVector) {
            // we can speed this up a lot
            ImmutableSparseVector iv = (ImmutableSparseVector) o;
            int n = 0;
            
            final int sz = size;
            final int osz = iv.size;
            int i = 0, j = 0;
            while (i < sz && j < osz) {
                final long k1 = keys[i];
                final long k2 = iv.keys[j];
                if (k1 == k2) {
                    n += 1;
                    i++;
                    j++;
                } else if (k1 < k2) {
                    i++;
                } else {
                    j++;
                }
            }
            
            return n;
        } else {
            return super.countCommonKeys(o);
        }
    }

    @Override
    public ImmutableSparseVector immutable() {
        return this;
    }
    
    @Override
    public MutableSparseVector mutableCopy() {
        return new MutableSparseVector(keys, Arrays.copyOf(values, size), size);
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
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @see MutableSparseVector#wrap(long[], double[])
     */
    public static ImmutableSparseVector wrap(long[] keys, double[] values) {
        return MutableSparseVector.wrap(keys, values).freeze();
    }

    /**
     * @see MutableSparseVector#wrap(long[], double[], int)
     */
    public static ImmutableSparseVector wrap(long[] keys, double[] values, int size) {
        return MutableSparseVector.wrap(keys, values, size).freeze();
    }
}
