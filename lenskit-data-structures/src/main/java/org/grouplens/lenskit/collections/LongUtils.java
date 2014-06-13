/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.*;

import java.util.*;

/**
 * Utilities for working with longs and collections of them from Fastutil.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class LongUtils {
    private LongUtils() {}

    /**
     * Pack longs into a sorted set.
     * @param longs A collection of longs.
     * @return An efficient sorted set containing the numbers in {@code longs}.
     */
    public static LongSortedSet packedSet(Collection<Long> longs) {
        return LongKeyDomain.fromCollection(longs, true).activeSetView();
    }

    /**
     * Pack longs into a sorted set.
     * @param longs An array of longs.  This array is copied, not wrapped.
     * @return An efficient sorted set containing the numbers in {@code longs}.
     */
    public static LongSortedSet packedSet(long... longs) {
        return LongKeyDomain.create(longs).activeSetView();
    }

    /**
     * Get a Fastutil {@link it.unimi.dsi.fastutil.longs.LongCollection} from a {@link java.util.Collection} of longs.
     * This method simply casts the collection, if possible, and returns a
     * wrapper otherwise.
     *
     * @param longs A collection of longs.
     * @return The collection as a {@link it.unimi.dsi.fastutil.longs.LongCollection}.
     */
    public static LongCollection asLongCollection(final Collection<Long> longs) {
        if (longs instanceof LongCollection) {
            return (LongCollection) longs;
        } else {
            return new LongCollectionWrapper(longs);
        }
    }

    /**
     * Get a Fastutil {@link it.unimi.dsi.fastutil.longs.LongSet} from a {@link java.util.Set} of longs.
     *
     * @param longs The set of longs.
     * @return {@code longs} as a fastutil {@link it.unimi.dsi.fastutil.longs.LongSet}. If {@code longs} is already
     *         a LongSet, it is cast.
     */
    public static LongSet asLongSet(final Set<Long> longs) {
        if (longs == null) {
            return null;
        } else if (longs instanceof LongSet) {
            return (LongSet) longs;
        } else {
            return new LongSetWrapper(longs);
        }
    }

    /**
     * Compute the set difference of two sets.
     *
     * @param items   The initial set
     * @param exclude The items to remove
     * @return The elements of {@var items} that are not in {@var exclude}.
     */
    public static LongSortedSet setDifference(LongSet items, LongSet exclude) {
        long[] data = new long[items.size()];
        final LongIterator iter = items.iterator();
        int i = 0;
        while (iter.hasNext()) {
            final long x = iter.nextLong();
            if (!exclude.contains(x)) {
                data[i++] = x;
            }
        }
        if (!(items instanceof LongSortedSet)) {
            Arrays.sort(data, 0, i);
        }
        // trim the array
        //CHECKSTYLE:OFF MagicNumber
        if (data.length * 2 > i * 3) {
            data = Arrays.copyOf(data, i);
        }
        //CHECKSTYLE:ON
        return new LongSortedArraySet(LongKeyDomain.wrap(data, i, true));
    }

    /**
     * Compute the size of the union of two sets.
     * @param a The first set.
     * @param b The second set.
     * @return The size of the union of the two sets.
     */
    public static int unionSize(LongSortedSet a, LongSortedSet b) {
        if (a instanceof LongSortedArraySet && b instanceof LongSortedArraySet) {
            LongKeyDomain da = ((LongSortedArraySet) a).getDomain();
            LongKeyDomain db = ((LongSortedArraySet) b).getDomain();
            if (da.isCompatibleWith(db)) {
                BitSet bits = (BitSet) da.getActiveMask().clone();
                bits.or(db.getActiveMask());
                return bits.cardinality();
            }
        }

        // we can't do fast bit operations, scan both sets instead
        LongIterator ait = a.iterator();
        LongIterator bit = b.iterator();
        boolean hasA = ait.hasNext();
        boolean hasB = bit.hasNext();
        long nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
        long nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
        int nshared = 0;
        while (hasA && hasB) {
            if (nextA < nextB) {
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
            } else if (nextB < nextA) {
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            } else {
                nshared += 1;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            }
        }
        return a.size() + b.size() - nshared;
    }

    /**
     * Compute the union of two sets.
     *
     * @param a The first set.
     * @param b The second set.
     * @return The elements of {@var items} that are not in {@var exclude}.
     */
    public static LongSortedSet setUnion(LongSortedSet a, LongSortedSet b) {
        if (a instanceof LongSortedArraySet && b instanceof LongSortedArraySet) {
            LongKeyDomain da = ((LongSortedArraySet) a).getDomain();
            LongKeyDomain db = ((LongSortedArraySet) b).getDomain();
            if (da.isCompatibleWith(db)) {
                LongKeyDomain result = da.clone();
                // we're in-package, go ahead and modify. our job to know it's safe.
                result.getActiveMask().or(db.getActiveMask());
                return result.activeSetView();
            }
        }

        long[] data = new long[unionSize(a, b)];

        LongIterator ait = a.iterator();
        LongIterator bit = b.iterator();
        boolean hasA = ait.hasNext();
        boolean hasB = bit.hasNext();
        long nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
        long nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
        int i = 0;
        while (hasA || hasB) {
            if (!hasB || nextA < nextB) {
                // use A
                data[i++] = nextA;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
            } else if (!hasA || nextB < nextA) {
                // use B
                data[i++] = nextB;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            } else {
                // they're both present and equal, use A but advance both
                // edge case: if one is missing but other is MAX_VALUE, it will go here
                // but that is fine, no harm will be done, as they're both MAX_VALUE
                data[i++] = nextA;
                hasA = ait.hasNext();
                nextA = hasA ? ait.nextLong() : Long.MAX_VALUE;
                hasB = bit.hasNext();
                nextB = hasB ? bit.nextLong() : Long.MAX_VALUE;
            }
        }
        assert i == data.length;

        return new LongSortedArraySet(LongKeyDomain.wrap(data, data.length, true));
    }

    /**
     * Selects a random subset of {@code n} longs from a given set of longs. If fewer than {@code n}
     * items can be selected the whole set is returned. 
     *
     *
     * @param set the set of items to select from
     * @param num The number of random items to add.
     * @param random a random number generator to be used.
     * @return An item selector that selects the items selected by {@code base} plus an additional
     * {@code nRandom} items.
     */
    public static LongSet randomSubset(LongSet set, int num, Random random) {
        return randomSubset(set, num, LongSortedSets.EMPTY_SET, random);
    }
    
    /**
     * Selects a random subset of {@code n} longs from a given set of longs such that no selected 
     * items is in a second set of longs. If fewer than {@code n} items can be selected the whole set is returned. 
     *
     *
     * @param set the set of items to select from
     * @param num The number of random items to add.
     * @param exclude a set of longs which must not be returned
     * @param rng a random number generator to be used.
     * @return An item selector that selects the items selected by {@code base} plus an additional
     * {@code nRandom} items.
     */
    public static LongSortedSet randomSubset(LongSet set, int num, LongSet exclude, Random rng) {
        // FIXME The RNG should come from configuration
        LongSet initial = exclude;
        LongList selected = new LongArrayList(num);
        int n = 0;
        LongIterator iter = set.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            if (exclude.contains(item)) {
                continue;
            }
            // algorithm adapted from Wikipedia coverage of Fisher-Yates shuffle
            // https://en.wikipedia.org/wiki/Fisher-Yates_shuffle
            int j = rng.nextInt(n + 1);
            n = n + 1;
            if (j < num) {
                if (j == selected.size()) {
                    selected.add(item);
                } else {
                    long old = selected.getLong(j);
                    if (selected.size() ==  num) {
                        selected.set(num - 1, old);
                    } else {
                        selected.add(old);
                    }
                    selected.set(j, item);
                }
            }
        }
        return LongUtils.packedSet(selected);

    }
    
    /**
     * Wrapper class that implements a {@link LongCollection} by delegating to
     * a {@link Collection}.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    private static class LongCollectionWrapper implements LongCollection {
        protected final Collection<Long> base;

        LongCollectionWrapper(Collection<Long> b) {
            base = b;
        }

        @Override
        public int size() {
            return base.size();
        }

        @Override
        public boolean contains(long key) {
            return base.contains(key);
        }

        @Override
        public LongIterator iterator() {
            return LongIterators.asLongIterator(base.iterator());
        }

        @Override
        public boolean add(Long item) {
            return base.add(item);
        }

        @Override
        public boolean addAll(Collection<? extends Long> items) {
            return base.addAll(items);
        }

        @Override
        public void clear() {
            base.clear();
        }

        @Override
        public boolean contains(Object item) {
            return base.contains(item);
        }

        @Override
        public boolean containsAll(Collection<?> items) {
            return base.containsAll(items);
        }

        @Override
        public boolean isEmpty() {
            return base.isEmpty();
        }

        @Override
        public boolean remove(Object item) {
            return base.remove(item);
        }

        @Override
        public boolean removeAll(Collection<?> items) {
            return base.removeAll(items);
        }

        @Override
        public boolean retainAll(Collection<?> items) {
            return base.retainAll(items);
        }

        @Override
        public Object[] toArray() {
            return base.toArray();
        }

        /**
         * {@inheritDoc}
         *
         * @deprecated see {@link LongCollection#longIterator()}
         */
        @Override
        @Deprecated
        public LongIterator longIterator() {
            return iterator();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return base.toArray(a);
        }

        @Override
        public long[] toLongArray() {
            final long[] items = new long[size()];
            LongIterators.unwrap(iterator(), items);
            return items;
        }

        @Override
        public long[] toLongArray(long[] a) {
            long[] output = a;
            if (output.length < size()) {
                output = new long[size()];
            }
            final int sz = LongIterators.unwrap(iterator(), output);
            if (sz < output.length) {
                output = Arrays.copyOf(output, sz);
            }
            return output;
        }

        @Override
        public long[] toArray(long[] a) {
            return toLongArray(a);
        }

        @Override
        public boolean add(long key) {
            return base.add(key);
        }

        @Override
        public boolean rem(long key) {
            return base.remove(key);
        }

        @Override
        public boolean addAll(LongCollection c) {
            return base.addAll(c);
        }

        @Override
        public boolean containsAll(LongCollection c) {
            return base.containsAll(c);
        }

        @Override
        public boolean removeAll(LongCollection c) {
            return base.removeAll(c);
        }

        @Override
        public boolean retainAll(LongCollection c) {
            return base.retainAll(c);
        }
    }

    private static class LongSetWrapper extends LongCollectionWrapper implements LongSet {
        LongSetWrapper(Collection<Long> base) {
            super(base);
        }

        @Override
        public boolean remove(long k) {
            return super.rem(k);
        }

        @Override
        public boolean equals(Object obj) {
            return base.equals(obj);
        }

        @Override
        public int hashCode() {
            return base.hashCode();
        }
    }
}
