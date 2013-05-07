/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;
import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.collections.*;
import org.grouplens.lenskit.scored.AbstractScoredId;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.symbols.Symbol;

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
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public abstract class SparseVector implements Iterable<VectorEntry>, Serializable {
    private static final long serialVersionUID = 1L;

    protected final long[] keys;
    protected final BitSet usedKeys;
    protected double[] values;
    protected final int domainSize; // How much of the key space is
    // actually used by this vector.

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
     * Construct a new vector from existing arrays.  It is assumed that the keys
     * are sorted and duplicate-free, and that the values is the same length. The
     * key array is the key domain, and all keys are considered used.
     * No new keys can be added to this vector.  Clients should call
     * the wrap() method rather than directly calling this constructor.
     *
     * @param ks The array of keys backing this vector. They must be sorted.
     * @param vs The array of values backing this vector.
     */
    SparseVector(long[] ks, double[] vs) {
        this(ks, vs, ks.length);
    }

    /**
     * Construct a new vector from existing arrays. It is assumed that
     * the keys are sorted and duplicate-free, and that the keys and
     * values both have at least {@var length} items.  The key set
     * and key domain are both set to the keys array.  Clients should
     * call the wrap() method rather than directly calling this
     * constructor.
     *
     * @param ks     The array of keys backing the vector. It must be sorted.
     * @param vs     The array of values backing the vector.
     * @param length Number of items to actually use.
     */
    SparseVector(long[] ks, double[] vs, int length) {
        assert MoreArrays.isSorted(ks, 0, length);
        keys = ks;
        values = vs;
        domainSize = length;
        usedKeys = new BitSet(length);
        for (int i = 0; i < length; i++) {
            usedKeys.set(i);
        }
    }

    /**
     * Construct a new vector from existing arrays. It is assumed that
     * the keys are sorted and duplicate-free, and that the keys and
     * values both have at least {@var length} items.  The key set
     * and key domain are both set to the keys array.  Clients should
     * call the wrap() method rather than directly calling this
     * constructor.
     *
     * @param ks     The array of keys backing the vector. It must be sorted.
     * @param vs     The array of values backing the vector.
     * @param length Number of items to actually use.
     * @param used   The used entry set.
     */
    SparseVector(long[] ks, double[] vs, int length, BitSet used) {
        assert MoreArrays.isSorted(ks, 0, length);
        keys = ks;
        values = vs;
        domainSize = length;
        usedKeys = used;
    }

    /**
     * Construct a new vector from the contents of a map. The key domain is the
     * key set of the map.  Therefore, no new keys can be added to this vector.
     *
     * @param ratings A map providing the values for the vector.
     */
    SparseVector(Long2DoubleMap ratings) {
        keys = ratings.keySet().toLongArray();
        domainSize = keys.length;
        Arrays.sort(keys);
        assert keys.length == ratings.size();
        assert MoreArrays.isSorted(keys, 0, domainSize);
        values = new double[keys.length];
        final int len = keys.length;
        for (int i = 0; i < len; i++) {
            values[i] = ratings.get(keys[i]);
        }
        usedKeys = new BitSet(domainSize);
        usedKeys.set(0, domainSize);
    }

    /**
     * Construct a new empty vector with the specified key domain.
     *
     * @param domain The key domain.
     */
    SparseVector(Collection<Long> domain) {
        LongSortedArraySet set;
        // since LSAS is immutable, we'll use its array if we can!
        if (domain instanceof LongSortedArraySet) {
            set = (LongSortedArraySet) domain;
        } else {
            set = new LongSortedArraySet(domain);
        }
        keys = set.unsafeArray();
        domainSize = domain.size();
        values = new double[domainSize];
        usedKeys = new BitSet(domainSize);
    }

    /**
     * Find the index of a particular key.
     *
     * @param key The key to search for.
     * @return The index, or a negative value if the key is not in the key domain.
     */
    protected int findIndex(long key) {
        return Arrays.binarySearch(keys, 0, domainSize, key);
    }

    /**
     * Query whether the vector contains an entry for the key in question.
     *
     * @param key The key to search for.
     * @return {@code true} if the key exists.
     */
    public boolean containsKey(long key) {
        final int idx = findIndex(key);
        return idx >= 0 && usedKeys.get(idx);
    }

    /**
     * Get the value for {@var key}.
     *
     * @param key the key to look up
     * @return the key's value (or {@link Double#NaN} if no such value
     *         exists)
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
     * @return the value (or {@var dft} if the key is not set to a value)
     */
    public double get(long key, double dft) {
        final int idx = findIndex(key);
        if (idx >= 0 && usedKeys.get(idx)) {
            return values[idx];
        } else {
            return dft;
        }
    }

    /**
     * Get the rating for the entry's key
     * @param e A {@code VectorEntry} with the key to look up
     * @return the key's value (or {@link Double#NaN} if no such value exists)
     */
    public double get(VectorEntry e) {
        return get(e.getKey());
    }

    /**
     * Fast iterator over all set entries (it can reuse entry objects).
     *
     * @return a fast iterator over all key/value pairs
     * @see #fastIterator(VectorEntry.State)
     * @see it.unimi.dsi.fastutil.longs.Long2DoubleMap.FastEntrySet#fastIterator()
     *      Long2DoubleMap.FastEntrySet.fastIterator()
     */
    public Iterator<VectorEntry> fastIterator() {
        return fastIterator(VectorEntry.State.SET);
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
    public Iterator<VectorEntry> fastIterator(VectorEntry.State state) {
        IntIterator iter;
        switch (state) {
        case SET:
            iter = new BitSetIterator(usedKeys, 0, domainSize);
            break;
        case UNSET: {
            BitSet unused = (BitSet) usedKeys.clone();
            unused.flip(0, domainSize);
            iter = new BitSetIterator(unused, 0, domainSize);
            break;
        }
        case EITHER: {
            iter = new IntIntervalList(0, domainSize).iterator();
            break;
        }
        default:
            throw new IllegalArgumentException("invalid entry state");
        }
        return new FastIterImpl(iter);
    }

    /**
     * Return an iterable view of this vector using a fast iterator. This method
     * delegates to {@link #fast(VectorEntry.State)} with state {@link VectorEntry.State#SET}.
     *
     * @return This object wrapped in an iterable that returns a fast iterator.
     * @see #fastIterator()
     */
    public Iterable<VectorEntry> fast() {
        return fast(VectorEntry.State.SET);
    }

    /**
     * Return an iterable view of this vector using a fast iterator.
     *
     * @param state The entries the resulting iterable should return.
     * @return This object wrapped in an iterable that returns a fast iterator.
     * @see #fastIterator(VectorEntry.State)
     * @since 0.11
     */
    public Iterable<VectorEntry> fast(final VectorEntry.State state) {
        return new Iterable<VectorEntry>() {
            @Override
            public Iterator<VectorEntry> iterator() {
                return fastIterator(state);
            }
        };
    }

    // The default iterator for this SparseVector iterates over
    // entries that are "used".  It uses an IterImpl class that
    // generates a new VectorEntry for every element returned, so the
    // client can safely keep around the VectorEntrys without concern
    // they will mutate, at some cost in speed.
    @Override
    public Iterator<VectorEntry> iterator() {
        return new IterImpl();
    }

    private class IterImpl implements Iterator<VectorEntry> {
        private BitSetIterator iter = new BitSetIterator(usedKeys);

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        @Nonnull
        public VectorEntry next() {
            int pos = iter.nextInt();
            return new VectorEntry(SparseVector.this, pos,
                                   keys[pos], values[pos], true);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // Given an int iterator, iterates over the elements of the <key,
    // value> pairs indexed by the int iterator.  For efficiency, may
    // reuse the VectorEntry returned at one step for a later step, so
    // the client should not keep around old VectorEntrys.
    private class FastIterImpl implements Iterator<VectorEntry> {
        private VectorEntry entry = new VectorEntry(SparseVector.this, -1, 0, 0, false);
        private IntIterator iter;

        public FastIterImpl(IntIterator positions) {
            iter = positions;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        @Nonnull
        public VectorEntry next() {
            int pos = iter.nextInt();
            boolean isSet = usedKeys.get(pos);
            double v = isSet ? values[pos] : Double.NaN;
            entry.set(pos, keys[pos], v, isSet);
            return entry;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Get the key domain for this vector. All keys used are in this
     * set.  The keys will be in sorted order.
     *
     * @return The key domain for this vector.
     */
    public LongSortedSet keyDomain() {
        return LongSortedArraySet.wrap(keys, domainSize);
    }

    /**
     * Get the set of keys of this vector. It is a subset of the key
     * domain.  The keys will be in sorted order.
     *
     * @return The set of keys used in this vector.
     */
    public LongSortedSet keySet() {
        return LongSortedArraySet.wrap(keys, domainSize, usedKeys);
    }

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
     * Get the collection of values of this vector.
     *
     * @return The collection of all values in this vector.
     */
    public DoubleCollection values() {
        DoubleArrayList lst = new DoubleArrayList(size());
        BitSetIterator iter = new BitSetIterator(usedKeys, 0, domainSize);
        while (iter.hasNext()) {
            int idx = iter.nextInt();
            lst.add(values[idx]);
        }
        return lst;
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
     * Get the size of this vector (the number of keys).
     *
     * @return The number of keys in the vector. This is at most the size of the
     *         key domain.
     */
    public int size() {
        return usedKeys.cardinality();
    }

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
     *
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
     *
     * @param o The other vector.
     * @return The number of keys appearing in both this and the other vector.
     */
    public int countCommonKeys(SparseVector o) {
        int n = 0;
        for (@SuppressWarnings("unused") Vectors.EntryPair pair : Vectors.pairedFast(this, o)) {
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
                if (!this.keySet().equals(vo.keySet())) {
                    return false;        // same keys
                }
                for (Vectors.EntryPair pair : Vectors.pairedFast(this, vo)) { // same values
                    if (pair.getValue1() != pair.getValue2()) { return false; }
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

    /**
     * Return whether this sparse vector has a channel stored under a
     * particular symbol.  (Symbols are sort of like names, but more
     * efficient.)
     *
     * @param channelSymbol the symbol under which the channel was
     *                      stored in the vector.
     * @return whether this vector has such a channel right now.
     */
    public abstract boolean hasChannel(Symbol channelSymbol);

    /**
     * Fetch the channel stored under a particular symbol.
     *
     * @param channelSymbol the symbol under which the channel was/is
     *                      stored in the vector.
     * @return the channel, which is itself a sparse vector.
     * @throws IllegalArgumentException if there is no channel under
     *                                  that symbol
     */
    public abstract SparseVector channel(Symbol channelSymbol);

    /**
     * Retrieve all symbols that map to side channels for this vector.
     * @return A set of symbols, each of which identifies a side channel
     *         of the vector.
     */
    public abstract Set<Symbol> getChannels();

    /**
     * Return a view of this vector as a {@code FastCollection} of
     * {@code ScoredId} objects.
     *
     * @return A fast collection containing this vector's keys and values as
     * {@code ScoredId} objects.
     */
    public FastCollection<ScoredId> scoredIds() {
        return new FastScoredIdCollectionImpl();
    }

    private class FastScoredIdCollectionImpl extends CopyingFastCollection<ScoredId> {

        private ScoredIdBuilder builder;

        public FastScoredIdCollectionImpl() {
            builder = new ScoredIdBuilder();
        }

        @Override
        protected ScoredId copy(ScoredId elt) {
            builder.clearChannels();
            builder.setId(elt.getId());
            builder.setScore(elt.getScore());
            for (Symbol s : elt.getChannels()) {
                builder.addChannel(s, elt.channel(s));
            }

            return builder.build();
        }

        @Override
        public int size() {
            return SparseVector.this.size();
        }

        @Override
        public Iterator<ScoredId> fastIterator() {
            return new FastIdIterImpl();
        }
    }

    private class FastIdIterImpl implements Iterator<ScoredId> {

        private Iterator<VectorEntry> entIter;
        private ScoredIdImpl id;

        public FastIdIterImpl() {
            entIter = fastIterator();
            id = new ScoredIdImpl();
        }

        @Override
        public boolean hasNext() {
            return entIter.hasNext();
        }

        @Override
        public ScoredId next() {
            id.setEntry(entIter.next());
            return id;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class ScoredIdImpl extends AbstractScoredId {

        private VectorEntry ent;

        @Override
        public long getId() {
            return ent.getKey();
        }

        @Override
        public double getScore() {
            return ent.getValue();
        }

        @Override
        public Set<Symbol> getChannels() {
            return SparseVector.this.getChannels();
        }

        @Override
        public double channel(Symbol s) {
            return SparseVector.this.channel(s).get(ent);
        }

        @Override
        public boolean hasChannel(Symbol s) {
            return SparseVector.this.hasChannel(s);
        }

        public void setEntry(VectorEntry e) {
            ent = e;
        }
    }
}
