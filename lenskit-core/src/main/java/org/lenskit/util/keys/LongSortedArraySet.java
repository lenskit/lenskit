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

import it.unimi.dsi.fastutil.longs.*;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * A sorted set of longs implemented using a sorted array.  It's much faster
 * than {@link LongArraySet} as it is able to use binary searches, while maintaining the space
 * compactness of array-backed sets.  Long sorted array sets are immutable.
 *
 * No orders are supported other than the natural ordering.
 *
 * @since 3.0 (a different version existed earlier.
 */
@Immutable
public class LongSortedArraySet extends AbstractLongSortedSet implements Serializable {
    private static final long serialVersionUID = 1L;

    private final SortedKeyIndex keys;

    /**
     * Construct a new long sorted array set from a key domain.
     * @param ks The key set storage.
     */
    public LongSortedArraySet(@Nonnull SortedKeyIndex ks) {
        keys = ks;
    }

    /**
     * Construct a new array set from a collection of items.
     *
     * @param items The set's contents.
     */
    public LongSortedArraySet(@Nonnull Collection<Long> items) {
        this(SortedKeyIndex.fromCollection(items));
    }

    /**
     * Construct a new array set from an array of items.
     * @param items The items to initialize the set with.  The items are copied, the array is
     *              not reused.
     */
    public LongSortedArraySet(long[] items) {
        this(SortedKeyIndex.create(items));
    }

    /**
     * Get the underlying key set implementation.  Warning, this allows you to modify the long
     * sorted array set.  This should only be used in data structure code.
     * @return The key set backing this set.
     */
    public SortedKeyIndex getDomain() {
        return keys;
    }

    @Override
    public LongComparator comparator() {
        return null;
    }

    @Override
    public long firstLong() {
        if (keys.size() > 0) {
            return keys.getKey(keys.getLowerBound());
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public long lastLong() {
        if (keys.size() > 0) {
            return keys.getKey(keys.getUpperBound() - 1);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public LongBidirectionalIterator iterator() {
        return keys.keyIterator();
    }

    @Override
    public LongBidirectionalIterator iterator(long key) {
        return keys.keyIterator(keys.findUpperBound(key));
    }

    @Override
    public LongSortedSet subSet(long startKey, long endKey) {
        int start = keys.findLowerBound(startKey);
        int end = keys.findLowerBound(endKey);
        return new LongSortedArraySet(keys.subIndex(start, end));
    }

    @Override
    public LongSortedSet headSet(long key) {
        int start = keys.getLowerBound();
        int end = keys.findLowerBound(key);
        return new LongSortedArraySet(keys.subIndex(start, end));
    }

    @Override
    public LongSortedSet tailSet(long key) {
        int start = keys.findLowerBound(key);
        int end = keys.getUpperBound();
        return new LongSortedArraySet(keys.subIndex(start, end));
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean contains(long key) {
        return keys.containsKey(key);
    }
}
