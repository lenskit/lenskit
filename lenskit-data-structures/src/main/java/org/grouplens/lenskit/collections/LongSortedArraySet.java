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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.longs.*;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * A sorted set of longs implemented using a sorted array.  It's much faster
 * than {@link LongArraySet} as it is able to use binary searches, while maintaining the space
 * compactness of array-backed sets.
 *
 * <p>No orders are supported other than the natural ordering.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class LongSortedArraySet extends AbstractLongSortedSet implements Serializable {
    private static final long serialVersionUID = 2L;

    private final LongKeySet keys;

    /**
     * Construct a new long sorted array set.
     * @param ks The key set storage.
     */
    LongSortedArraySet(@Nonnull LongKeySet ks) {
        keys = ks;
    }

    /**
     * Construct a new array set from a collection of items.
     *
     * @param items The set's contents.
     */
    public LongSortedArraySet(@Nonnull Collection<Long> items) {
        this(LongKeySet.fromCollection(items));
    }

    /**
     * Construct a new array set from an array of items.
     * @param items The items to initialize the set with.  The items are copied, the array is
     *              not reused.
     */
    public LongSortedArraySet(long[] items) {
        this(LongKeySet.fromCollection(LongArrayList.wrap(items)));
    }

    /**
     * Get the underlying key set implementation.  Warning, this allows you to modify the long
     * sorted array set.  This should only be used in data structure code.
     * @return The key set backing this set.
     */
    public LongKeySet getKeySet() {
        return keys;
    }

    @Override
    public LongComparator comparator() {
        return null;
    }

    @Override
    public long firstLong() {
        return keys.firstActiveKey();
    }

    @Override
    public long lastLong() {
        return keys.lastActiveKey();
    }

    @Override
    public LongBidirectionalIterator iterator(long key) {
        int index = keys.upperBound(key);
        return keys.keyIterator(keys.activeIndexIterator(index));
    }

    @Override
    public LongSortedSet subSet(long startKey, long endKey) {
        int start = keys.lowerBound(startKey);
        int end = keys.lowerBound(endKey);
        return new LongSortedArraySet(keys.subset(start, end));
    }

    @Override
    public LongSortedSet headSet(long key) {
        int start = keys.getStartIndex();
        int end = keys.lowerBound(key);
        return new LongSortedArraySet(keys.subset(start, end));
    }

    @Override
    public LongSortedSet tailSet(long key) {
        int start = keys.lowerBound(key);
        int end = keys.getEndIndex();
        return new LongSortedArraySet(keys.subset(start, end));
    }

    @Override
    public LongBidirectionalIterator iterator() {
        return keys.keyIterator(keys.activeIndexIterator());
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean contains(long key) {
        return keys.getIndexIfActive(key) >= 0;
    }

    @Override
    public boolean rem(long k) {
        throw new UnsupportedOperationException();
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
        return new LongSortedArraySet(LongKeySet.wrap(data, 0, i, true));
    }
}
