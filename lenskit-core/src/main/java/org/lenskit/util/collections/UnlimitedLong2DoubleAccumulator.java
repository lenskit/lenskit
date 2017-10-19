/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.collections;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.longs.*;

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
