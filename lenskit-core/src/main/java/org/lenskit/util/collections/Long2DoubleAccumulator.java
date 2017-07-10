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
