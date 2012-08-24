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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import static org.grouplens.lenskit.vectors.VectorEntry.State;

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
 * @compat Public
 */
public abstract class SparseVector implements Iterable<VectorEntry> {
    private transient volatile Double norm;
    private transient volatile Double sum;
    private transient volatile Double mean;
    private transient volatile Integer hashCode;

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
     * Get the rating for {@var key}.
     *
     * @param key the key to look up
     * @return the key's value (or {@link Double#NaN} if no such value exists)
     * @see #get(long, double)
     */
    public double get(long key) {
        return get(key, Double.NaN);
    }

    /**
     * Get the rating for {@var key}.
     *
     * @param key the key to look up
     * @param dft The value to return if the key is not in the vector
     * @return the value (or {@var dft} if no such key exists)
     */
    public abstract double get(long key, double dft);

    /**
     * Query whether the vector contains an entry for the key in question.
     *
     * @param key The key to search for.
     * @return {@code true} if the key exists.
     */
    public abstract boolean containsKey(long key);

    /**
     * Iterate over all entries.
     *
     * @return an iterator over all key/value pairs.
     */
    @Override
    public abstract Iterator<VectorEntry> iterator();

    /**
     * Fast iterator over all set entries (it can reuse entry objects).
     *
     * @return a fast iterator over all key/value pairs
     * @see #fastIterator(State)
     * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.FastEntrySet#fastIterator()
     *      Long2DoubleMap.FastEntrySet.fastIterator()
     */
    public Iterator<VectorEntry> fastIterator() {
        return fastIterator(State.SET);
    }

    /**
     * Fast iterator over entries (it can reuse entry objects).
     *
     * @param state The state of entries to iterate.
     * @return a fast iterator over all key/value pairs
     * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.FastEntrySet#fastIterator()
     *      Long2DoubleMap.FastEntrySet.fastIterator()
     * @since 0.11
     */
    public abstract Iterator<VectorEntry> fastIterator(State state);

    /**
     * Return an iterable view of this vector using a fast iterator. This method
     * delegates to {@link #fast(State)} with state {@link State#SET}.
     *
     * @return This object wrapped in an iterable that returns a fast iterator.
     * @see #fastIterator()
     */
    public Iterable<VectorEntry> fast() {
        return fast(State.SET);
    }

    /**
     * Return an iterable view of this vector using a fast iterator.
     *
     * @param state The entries the resulting iterable should return.
     * @return This object wrapped in an iterable that returns a fast iterator.
     * @see #fastIterator(State)
     * @since 0.11
     */
    public Iterable<VectorEntry> fast(final State state) {
        return new Iterable<VectorEntry>() {
            @Override
            public Iterator<VectorEntry> iterator() {
                return fastIterator(state);
            }
        };
    }

    /**
     * Get the set of keys of this vector. It is a subset of the key domain.
     *
     * @return The set of keys used in this vector.
     */
    public abstract LongSortedSet keySet();

    /**
     * Get the key domain for this vector. All keys used are in this set.
     *
     * @return The key domain for this vector.
     */
    public abstract LongSortedSet keyDomain();

    /**
     * Return the keys of this vector sorted by value.
     *
     * @return A list of keys in nondecreasing order of value.
     * @see #keysByValue(boolean)
     */
    public LongArrayList keysByValue() {
        return keysByValue(false);
    }

    /**
     * Get the keys of this vector sorted by value.
     *
     * @param decreasing If {@var true}, sort in decreasing order.
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
                    if (c != 0) {
                        return c;
                    } else {
                        return Longs.compare(k1, k2);
                    }
                }
            };
        } else {
            cmp = new AbstractLongComparator() {
                @Override
                public int compare(long k1, long k2) {
                    int c = Double.compare(get(k1), get(k2));
                    if (c != 0) {
                        return c;
                    } else {
                        return Longs.compare(k1, k2);
                    }
                }
            };
        }

        LongArrays.quickSort(skeys, cmp);
        return LongArrayList.wrap(skeys);
    }

    /**
     * Get the collection of values of this vector.
     *
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
     *
     * @return {@code true} if the vector is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Compute and return the L2 norm (Euclidian length) of the vector.
     *
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
     * Compute and return the L1 norm (sum) of the vector.
     *
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
     * Compute and return the mean of the vector's values.
     *
     * @return the mean of the vector
     */
    public double mean() {
        if (mean == null) {
            final int sz = size();
            mean = sz > 0 ? sum() / sz : 0;
        }
        return mean;
    }

    /**
     * Compute the dot product between two vectors.
     * @param o The other vector.
     * @return The dot (inner) product between this vector and {@var o}.
     */
    public double dot(SparseVector o) {
        double dot = 0;
        for (Vectors.EntryPair pair : Vectors.pairedFast(this, o)) {
            dot += pair.getValue1() * pair.getValue2();
        }
        return dot;
    }

    /**
     * Count the common keys between two vectors.
     * @param o The other vector.
     * @return The number of keys appearing in both this and the other vector.
     */
    public int countCommonKeys(SparseVector o) {
        int n = 0;
        for (Vectors.EntryPair pair : Vectors.pairedFast(this, o)) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        Function<VectorEntry, String> label = new Function<VectorEntry, String>() {
            @Override
            public String apply(VectorEntry e) {
                return String.format("%d: %.3f", e.getKey(), e.getValue());
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
