/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.collections;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Scored item accumulator with no upper bound.
 */
public final class UnlimitedLong2DoubleAccumulator implements Long2DoubleAccumulator {
    private Long2DoubleMap entries;

    public UnlimitedLong2DoubleAccumulator() {}

    @Override
    public boolean isEmpty() {
        return entries == null || entries.size() == 0;
    }

    @Override
    public int size() {
        return entries == null ? 0 : entries.size();
    }

    @Override
    public void put(long item, double score) {
        if (entries == null) {
            entries = new Long2DoubleOpenHashMap();
        }
        entries.put(item, score);
    }

    @Override
    public MutableSparseVector finishVector() {
        if (entries == null) {
            return MutableSparseVector.create();
        }

        MutableSparseVector vec = MutableSparseVector.create(entries);
        entries = null;
        return vec;
    }

    @Override
    public Long2DoubleMap finishMap() {
        if (entries == null) {
            return Long2DoubleMaps.EMPTY_MAP;
        }
        Long2DoubleMap map = entries;
        entries = null;
        return map;
    }

    @Override
    public LongSet finishSet() {
        if (entries == null) {
            return LongSets.EMPTY_SET;
        }

        LongSet set = new LongOpenHashSet(entries.keySet());
        entries = null;
        return set;
    }

    @Override
    public LongList finishList() {
        if (entries == null) {
            return LongLists.EMPTY_LIST;
        }

        long[] longs = entries.keySet().toLongArray();
        LongArrays.quickSort(longs, new AbstractLongComparator() {
            @Override
            public int compare(long k1, long k2) {
                return Doubles.compare(entries.get(k2), entries.get(k1));
            }
        });
        entries = null;
        return new LongArrayList(longs);
    }
}
