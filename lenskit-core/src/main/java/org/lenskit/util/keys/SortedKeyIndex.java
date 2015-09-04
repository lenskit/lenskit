/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.keys;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.collections.MoreArrays;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.*;

/**
 * Implement an index of long keys, sorted by key.  Keys can be mapped back to integer indexes (contiguous and 0-based)
 * and vice versa.
 *
 * @since 3.0
 * @compat Private
 */
@Immutable
public abstract class SortedKeyIndex implements KeyIndex, Serializable {
    //region Factory methods
    /**
     * Wrap a key array (with a specified size) into a key set.
     * @param keys The key array.  This array must be sorted, and must not contain duplicates.  For
     *             efficiency, this condition is not checked unless assertions are enabled.  Since
     *             this method is only intended to be used when implementing test cases or other
     *             data structures, callers of this method should ensure sortedness and
     *             throw the appropriate exception.
     * @param size The length of the array to actually use.
     * @return The key set.
     */
    public static SortedKeyIndex wrap(long[] keys, int size) {
        Preconditions.checkArgument(size <= keys.length, "size too large");
        assert MoreArrays.isSorted(keys, 0, size);
        return new FullSortedKeyIndex(keys, 0, size);
    }

    /**
     * Create a key set from a collection of keys.
     *
     * @param keys            The key collection.
     * @return The key set.
     */
    public static SortedKeyIndex fromCollection(Collection<Long> keys) {
        if (keys instanceof LongSortedArraySet) {
            return ((LongSortedArraySet) keys).getDomain();
        } else {
            return fromIterator(keys.size(), keys.iterator());
        }
    }

    /**
     * Build a key domain from an iterator.
     * @param nmax The maximum number of items to include.
     * @param keys The key iterator.  The iterator must return no more than {@code nmax} items.
     * @return The key domain.
     */
    private static SortedKeyIndex fromIterator(int nmax, Iterator<Long> keys) {
        // 2 options to build the array. Invariant: exactly one is non-null.
        long[] keyArray = null;
        int[] smallKeyArray = new int[nmax];

        int pos = 0;
        LongIterator iter = LongIterators.asLongIterator(keys);
        while (iter.hasNext()) {
            long k = iter.nextLong();
            if (smallKeyArray != null && k >= Integer.MIN_VALUE && k <= Integer.MAX_VALUE) {
                // still building a small key array
                smallKeyArray[pos] = (int) k;
            } else {
                if (keyArray == null) {
                    assert smallKeyArray != null;
                    // we need to upgrade
                    keyArray = new long[nmax];
                    for (int i = 0; i < pos; i++) {
                        keyArray[i] = smallKeyArray[i];
                    }
                    smallKeyArray = null;
                }
                keyArray[pos] = k;
            }
            pos++;
        }
        if (iter.hasNext()) {
            throw new ConcurrentModificationException("iterator size changed during scan");
        }

        if (keyArray != null) {
            assert pos == keyArray.length;
            assert smallKeyArray == null;
            Arrays.sort(keyArray);
            int size = MoreArrays.deduplicate(keyArray, 0, keyArray.length);
            return new FullSortedKeyIndex(keyArray, 0, size);
        } else {
            assert smallKeyArray != null;
            assert pos == smallKeyArray.length;
            Arrays.sort(smallKeyArray);
            int size = MoreArrays.deduplicate(smallKeyArray, 0, smallKeyArray.length);
            return new CompactSortedKeyIndex(smallKeyArray, 0, size);
        }
    }

    /**
     * Create a key set with some keys.  All keys are initially active.
     * @param keys The keys.
     * @return The key set.
     */
    public static SortedKeyIndex create(long... keys) {
        // the delegation goes this way to minimize the number of array copies
        return fromCollection(LongArrayList.wrap(keys));
    }

    /**
     * Create an empty key domain.
     * @return An empty key domain.
     */
    public static SortedKeyIndex empty() {
        // since empty domains are immutable, use a singleton
        return EMPTY_DOMAIN;
    }

    private static final SortedKeyIndex EMPTY_DOMAIN = wrap(new long[0], 0);
    //endregion

    private static final long serialVersionUID = 2L;

    /**
     * The lower bound (inclusive) of the indexes in the index.  This is used to implement subviews.
     */
    final int lowerBound;
    /**
     * The upper bound (exclusive) of the indexes in the index.  This is used to implement subviews.
     */
    final int upperBound;

    SortedKeyIndex(int lower, int upper) {
        lowerBound = lower;
        upperBound = upper;
    }

    /**
     * Get the domain size of this set.
     * @return The domain size.
     */
    @Override
    public int size() {
        return upperBound - lowerBound;
    }

    /**
     * Get the lower bound of this index.
     * @return The index's lower bound.
     */
    @Override
    public int getLowerBound() {
        return lowerBound;
    }

    /**
     * Get the upper bound of this index.
     * @return The index's upper bound.
     */
    @Override
    public int getUpperBound() {
        return upperBound;
    }

    /**
     * Create a view of a subset of this index.
     * @param lb The index of the lower bound of the subset (inclusive).
     * @param ub The index of the upper bound of the subset (exclusive);
     */
    public abstract SortedKeyIndex subIndex(int lb, int ub);

    /**
     * Get the index for a key.
     *
     * @param key The key.
     * @return The index, or a negative value if the key is not in the domain.  Such a negative
     *         value is the <em>insertion point</em>, as defined by
     *         {@link Arrays#binarySearch(long[], int, int, long)}.
     */
    @Override
    public abstract int tryGetIndex(long key);

    @Override
    public int getIndex(long key) {
        int idx = tryGetIndex(key);
        if (idx < 0) {
            throw new IllegalArgumentException("key " + key + " is not in the key index");
        }
        return idx;
    }

    /**
     * Get the upper bound, the first index whose key is greater than the specified key.
     *
     * @param key The key to search for.
     * @return The index of the first key greater than the specified key, or {@link #getUpperBound()} if the key
     * is the last key in the domain.
     */
    public int findUpperBound(long key) {
        int index = tryGetIndex(key);
        if (index >= 0) {
            // the key is there, advance by 1
            return index + 1;
        } else {
            // the key is not there, the insertion point is > key
            return -index - 1;
        }
    }

    /**
     * Get the lower bound, the first index whose key is greater than or equal to the specified key.
     * This method is paired with {@link #findUpperBound(long)}; the interval
     * {@code [findLowerBound(k),findUpperBound(k))} contains the index of {@code k}, if the key is in the
     * domain, and is empty if the key is not in the domain.
     *
     * @param key The key to search for.
     * @return The index of the first key greater than or equal to {@code key}; will be {@link #getLowerBound()} if
     * {@code key} is less than or equal to the lowest key.
     */
    public int findLowerBound(long key) {
        int index = tryGetIndex(key);
        if (index >= 0) {
            // the key is there, first index is >=
            return index;
        } else {
            // the key is not there, the insertion point is > key
            return -index - 1;
        }
    }

    /**
     * Query whether this set contains the specified key in its domain.
     * @param key The key.
     * @return {@code true} if the key is in the domain.
     */
    @Override
    public boolean containsKey(long key) {
        int idx = tryGetIndex(key);
        return idx >= lowerBound && idx < upperBound;
    }

    /**
     * Get the key at an index.
     * @param idx The index to query.
     * @return The key at the specified index.
     */
    @Override
    public abstract long getKey(int idx);

    /**
     * Create an iterator over keys.
     * @return An iterator over the keys corresponding to the iterator's indexes.
     */
    public LongBidirectionalIterator keyIterator() {
        return keyIterator(lowerBound);
    }

    /**
     * Create an iterator over keys.
     * @param initial The initial index (the first thing to be returned by {@link Iterator#next()}).
     * @return An iterator over the keys corresponding to the iterator's indexes.
     */
    public LongBidirectionalIterator keyIterator(int initial) {
        if (initial < lowerBound || initial > upperBound) {
            throw new IndexOutOfBoundsException("initial index " + initial + " out of range");
        }
        return new KeyIter(initial);
    }

    /**
     * Get a view of the index as a set.
     * @return The set of keys.
     */
    public LongSortedSet keySet() {
        return new LongSortedArraySet(this);
    }

    /**
     * Get the key set's list of keys (domain) as a list.
     * @return A list of all keys in the key domain.
     */
    @Override
    public LongList getKeyList() {
        return new KeyList();
    }

    @Override
    public SortedKeyIndex frozenCopy() {
        return this;
    }

    //region Iterators and lists
    private class KeyIter extends AbstractLongBidirectionalIterator {
        private int position;

        public KeyIter(int pos) {
            position = pos;
        }

        @Override
        public boolean hasPrevious() {
            return position > lowerBound;
        }

        @Override
        public boolean hasNext() {
            return position < upperBound;
        }

        @Override
        public long nextLong() {
            if (position < upperBound) {
                // position is the next thing to return by 'next'
                long k = getKey(position);
                position += 1;
                return k;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public long previousLong() {
            if (position > lowerBound) {
                // since position is *next* thing to return by 'next', it is one past *last* thing returned.
                position -= 1;
                return getKey(position);
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    private class KeyList extends AbstractLongList {
        @Override
        public int size() {
            return SortedKeyIndex.this.size();
        }

        @Override
        public long getLong(int i) {
            Preconditions.checkElementIndex(i, SortedKeyIndex.this.size());
            return getKey(getLowerBound() + i);
        }
    }
    //endregion
}
