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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Accumulate a sorted list of scored IDs.
 */
public interface Long2DoubleAccumulator {
    /**
     * Query whether the accumulator is empty.
     *
     * @return {@code true} if the accumulator has no items.
     */
    boolean isEmpty();

    /**
     * Get the number of items in the accumulator.
     *
     * @return The number of items accumulated so far, up to the maximum items
     *         desired from the accumulator.
     */
    int size();

    /**
     * Put a new item in the accumulator. Putting the same item twice does
     * <strong>not</strong> replace the previous entry - it adds a new entry
     * with the same ID.
     *
     * @param item  The item to add to the accumulator.
     * @param score The item's score.
     */
    void put(long item, double score);

    /**
     * Add all items from a map to the accumulator.
     * @param map The map.
     */
    default void putAll(Long2DoubleMap map) {
        for (Long2DoubleMap.Entry e: Long2DoubleMaps.fastIterable(map)) {
            put(e.getLongKey(), e.getDoubleValue());
        }
    }

    /**
     * Accumulate the scores into a map and reset the accumulator.
     *
     * After this method is called, the accumulator is ready for another accumulation.
     *
     * @return A map of items.
     */
    Long2DoubleMap finishMap();

    /**
     * Accumulate the scored items into a set.
     * @return The set of items accumulated.
     */
    LongSet finishSet();

    /**
     * Accumulate the scored items into a list.
     * @return The list of items accumulated, in decreasing order of score.
     */
    LongList finishList();
}
