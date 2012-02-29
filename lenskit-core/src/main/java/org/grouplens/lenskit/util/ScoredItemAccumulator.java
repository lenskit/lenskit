package org.grouplens.lenskit.util;

import org.grouplens.lenskit.collections.ScoredLongList;

/**
 * Accumulate a sorted list of scored items.
 * @author Michael Ekstrand
 */
public interface ScoredItemAccumulator {
    /**
     * Query whether the accumulator is empty.
     * @return <tt>true</tt> if the accumulator has no items.
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
     * @param item The item to add to the accumulator.
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
