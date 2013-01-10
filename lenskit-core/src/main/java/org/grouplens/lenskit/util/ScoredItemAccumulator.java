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
package org.grouplens.lenskit.util;

import org.grouplens.lenskit.collections.ScoredLongList;

/**
 * Accumulate a sorted list of scored items.
 *
 * @author Michael Ekstrand
 */
public interface ScoredItemAccumulator {
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
     * Accumulate the scores into a sorted scored list and reset the
     * accumulator.  Items are sorted in decreasing order of score.
     *
     * <p>After this method is called, the accumulator is ready for another
     * accumulation.
     *
     * @return The sorted, scored list of items.
     */
    ScoredLongList finish();
}
