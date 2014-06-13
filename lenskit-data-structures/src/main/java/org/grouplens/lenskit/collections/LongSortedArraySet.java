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

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * A sorted set of longs implemented using a sorted array.  It's much faster
 * than {@link LongArraySet} as it is able to use binary searches, while maintaining the space
 * compactness of array-backed sets.
 *
 * <p>No orders are supported other than the natural ordering.
 *
 * <p>A long sorted array set is a read-only view of its key set.  It does not take ownership of
 * the key set.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class LongSortedArraySet extends AbstractLongSortedSet implements Serializable {
    private static final long serialVersionUID = 2L;

    final LongKeyDomain keys;
    private final int minIndex;
    private final int maxIndex;

    /**
     * Construct a new long sorted array set.
     * @param ks The key set storage.
     */
    LongSortedArraySet(@Nonnull LongKeyDomain ks) {
        this(ks, 0, ks.domainSize());
    }

    /**
     * Construct a new long sorted array set.
     * @param ks The key set storage.
     */
    LongSortedArraySet(@Nonnull LongKeyDomain ks, int min, int max) {
        assert min >= 0;
        assert max >= min;
        keys = ks;
        keys.acquire();
        minIndex = min;
        maxIndex = max;
    }

    /**
     * Construct a new array set from a collection of items.
     *
     * @param items The set's contents.
     */
    LongSortedArraySet(@Nonnull Collection<Long> items) {
        this(LongKeyDomain.fromCollection(items));
    }

    /**
     * Construct a new array set from an array of items.
     * @param items The items to initialize the set with.  The items are copied, the array is
     *              not reused.
     */
    LongSortedArraySet(long[] items) {
        this(LongKeyDomain.create(items));
    }

    /**
     * Get the underlying key set implementation.  Warning, this allows you to modify the long
     * sorted array set.  This should only be used in data structure code.
     * @return The key set backing this set.
     */
    LongKeyDomain getDomain() {
        return keys;
    }

    @Override
    public LongComparator comparator() {
        return null;
    }

    @Override
    public long firstLong() {
        int idx = keys.getActiveMask().nextSetBit(minIndex);
        if (idx >= minIndex && idx < maxIndex) {
            return keys.getKey(idx);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public long lastLong() {
        for (int idx = maxIndex - 1; idx >= minIndex; --idx) {
            if (keys.indexIsActive(idx)) {
                return keys.getKey(idx);
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public LongBidirectionalIterator iterator(long key) {
        int index = Math.max(keys.upperBound(key), minIndex);
        return keys.keyIterator(keys.activeIndexIterator(minIndex, maxIndex, index));
    }

    @Override
    public LongSortedSet subSet(long startKey, long endKey) {
        int start = keys.lowerBound(startKey);
        int end = keys.lowerBound(endKey);
        return new LongSortedArraySet(keys, start, end);
    }

    @Override
    public LongSortedSet headSet(long key) {
        int start = minIndex;
        int end = keys.lowerBound(key);
        return new LongSortedArraySet(keys, start, end);
    }

    @Override
    public LongSortedSet tailSet(long key) {
        int start = keys.lowerBound(key);
        int end = maxIndex;
        return new LongSortedArraySet(keys, start, end);
    }

    @Override
    public LongBidirectionalIterator iterator() {
        // FIXME Don't allow the iterator to go backwards from the min!
        return keys.keyIterator(keys.activeIndexIterator(minIndex, maxIndex, minIndex));
    }

    @Override
    public int size() {
        if (minIndex == 0 && maxIndex == keys.domainSize()) {
            return keys.size();
        } else {
            BitSet bits = new BitSet(maxIndex);
            bits.set(minIndex, maxIndex);
            bits.and(keys.getActiveMask());
            return bits.cardinality();
        }
    }

    @Override
    public boolean contains(long key) {
        int idx = keys.getIndexIfActive(key);
        return idx >= minIndex && idx < maxIndex;
    }
}
