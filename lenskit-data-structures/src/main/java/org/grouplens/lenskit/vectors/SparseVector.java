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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.Pointer;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Longs;

/**
 * Read-only interface to sparse vectors.
 * 
 * <p>
 * This vector class works a lot like a map, but it also caches some
 * commonly-used statistics. The values are stored in parallel arrays sorted by
 * key. This allows fast lookup and sorted iteration. All iterators access the
 * items in key order.
 * 
 * <p>
 * Vectors have a <i>key domain</i>, which is a set containing all valid keys in
 * the vector. This key domain is fixed at construction; mutable vectors cannot
 * set values for keys not in this domain. Thinking of the vector as a function
 * from longs to doubles, the key domain would actually be the codomain, and the
 * key set the algebraic domain, but that gets cumbersome to write in code. So
 * think of the key domain as the domain from which valid keys are drawn.
 * 
 * <p>
 * This class provides a <em>read-only</em> interface to sparse vectors. It may
 * actually be a {@link MutableSparseVector}, so the data may be modified by
 * code elsewhere that has access to the mutable representation. For sparse
 * vectors that are guaranteed to be unchanging, see
 * {@link ImmutableSparseVector}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public abstract class SparseVector implements Iterable<Long2DoubleMap.Entry> {
    private volatile transient Double norm;
    private volatile transient Double sum;
    private volatile transient Double mean;
    private volatile transient Integer hashCode;

    /**
     * Clear all cached/memoized values. This must be called any time the vector
     * is modified.
     */
    protected void clearCachedValues() {
        norm = null;
        sum = null;
        mean = null;
        hashCode = null;
    }

    /**
     * Get the rating for <var>key</var>.
     * @param key the key to look up
     * @return the key's value (or {@link Double#NaN} if no such value exists)
     * @see #get(long, double)
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
    public abstract double get(long key, double dft);

    /**
     * Query whether the vector contains an entry for the key in question.
     *
     * @param key The key to search for.
     * @return <tt>true</tt> if the key exists.
     */
    public abstract boolean containsKey(long key);

    /**
     * Iterate over all entries.
     * @return an iterator over all key/value pairs.
     */
    @Override
    public abstract Iterator<Long2DoubleMap.Entry> iterator();

    /**
     * Fast iterator over all entries (it can reuse entry objects).
     * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.FastEntrySet#fastIterator()
     *         Long2DoubleMap.FastEntrySet.fastIterator()
     * @return a fast iterator over all key/value pairs
     */
    public abstract Iterator<Long2DoubleMap.Entry> fastIterator(); 

    /**
     * Return an iterable view of this vector using a fast iterator.
     * @return This object wrapped in an iterable that returns a fast iterator.
     * @see #fastIterator()
     */
    public Iterable<Long2DoubleMap.Entry> fast() {
        return new Iterable<Long2DoubleMap.Entry>() {
            @Override
            public Iterator<Long2DoubleMap.Entry> iterator() {
                return fastIterator();
            }
        };
    }

    /**
     * Get the set of keys of this vector. It is a subset of the key domain.
     * @return The set of keys used in this vector.
     */
    public abstract LongSortedSet keySet();
    
    /**
     * Get the key domain for this vector. All keys used are in this set.
     * @return The key domain for this vector.
     */
    public abstract LongSortedSet keyDomain();

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
        long[] skeys = keySet().toLongArray();

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

    /**
     * Get the collection of values of this vector.
     * @return The collection of all values in this vector.
     */
    public abstract DoubleCollection values();

    /**
     * Get the size of this vector (the number of keys).
     * 
     * @return The number of keys in the vector. This is at most the size of the
     *         key domain.
     */
    public abstract int size();

    /**
     * Query whether this vector is empty.
     * @return <tt>true</tt> if the vector is empty.
     */
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
            final int sz = size();
            mean = sz > 0 ? sum() / sz: 0;
        }
        return mean;
    }
    
    public double dot(SparseVector o) {
        double dot = 0;
        
        Pointer<Entry> p1 = CollectionUtils.pointer(fastIterator());
        Pointer<Entry> p2 = CollectionUtils.pointer(o.fastIterator());
        
        while (!p1.isAtEnd() && !p2.isAtEnd()) {
            final long k1 = p1.get().getLongKey();
            final long k2 = p2.get().getLongKey();
            if (k1 == k2) {
                dot += p1.get().getDoubleValue() * p2.get().getDoubleValue();
                p1.advance();
                p2.advance();
            } else if (k1 < k2) {
                p1.advance();
            } else {
                p2.advance();
            }
        }
        
        return dot;
    }
    
    public int countCommonKeys(SparseVector o) {
        int n = 0;
        
        Pointer<Entry> p1 = CollectionUtils.pointer(fastIterator());
        Pointer<Entry> p2 = CollectionUtils.pointer(o.fastIterator());
        
        while (!p1.isAtEnd() && !p2.isAtEnd()) {
            final long k1 = p1.get().getLongKey();
            final long k2 = p2.get().getLongKey();
            if (k1 == k2) {
                n++;
                p1.advance();
                p2.advance();
            } else if (k1 < k2) {
                p1.advance();
            } else {
                p2.advance();
            }
        }
        
        return n;
    }
    
    @Override
    public String toString() {
        Function<Long2DoubleMap.Entry, String> label = new Function<Long2DoubleMap.Entry, String>() {
            @Override
            public String apply(Long2DoubleMap.Entry e) {
                return String.format("%d: %.3f", e.getLongKey(), e.getDoubleValue());
            }
        };
        return "{" + StringUtils.join(Iterators.transform(fastIterator(), label), ", ") + "}";
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
                return keySet().equals(vo.keySet()) && values().equals(vo.values());
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = keySet().hashCode() ^ values().hashCode();
        }
        return hashCode;
    }

    /**
     * Return an immutable snapshot of this sparse vector. The new vector's key
     * domain will be equal to the {@link #keySet()} of this vector.
     * 
     * @return An immutable sparse vector whose contents are the same as this
     *         vector. If the vector is already immutable, the returned object
     *         may be identical.
     */
    public abstract ImmutableSparseVector immutable();

    /**
     * Return a mutable copy of this sparse vector. The key domain of the
     * mutable vector will be the same as this vector's key domain.
     * 
     * @return A mutable sparse vector which can be modified without modifying
     *         this vector.
     */
    public abstract MutableSparseVector mutableCopy();
}
