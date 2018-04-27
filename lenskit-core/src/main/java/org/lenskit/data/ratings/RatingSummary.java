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
package org.lenskit.data.ratings;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import java.io.Serializable;

/**
 * A summary of the ratings data.
 *
 * @since 3.0
 */
@Shareable
@DefaultProvider(RatingSummaryProvider.class)
public class RatingSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double globalMean;
    private final SortedKeyIndex itemIndex;
    private final double[] itemOffsets;
    private final int[] itemCounts;

    /**
     * Construct a new rating summary.
     * @param mean The global mean rating.
     * @param keys The item IDs.
     * @param offsets The item mean offsets.
     * @param counts The item rating counts.
     */
    RatingSummary(double mean, SortedKeyIndex keys, double[] offsets, int[] counts) {
        Preconditions.checkArgument(offsets.length == keys.size(), "offset array length");
        Preconditions.checkArgument(counts.length == keys.size(), "count array length");
        globalMean = mean;
        itemIndex = keys;
        itemOffsets = offsets;
        itemCounts = counts;
    }

    /**
     * Create a rating summary from a DAO.
     * @param dao The events.
     * @return The rating summary.
     */
    public static RatingSummary create(DataAccessObject dao) {
        return new RatingSummaryProvider(dao).get();
    }

    public double getGlobalMean() {
        return globalMean;
    }

    public LongSet getItems() {
        return itemIndex.keySet();
    }

    /**
     * Get the mean rating for an item.
     * @param item The item.
     * @return The item's mean rating, or {@link Double#NaN} if the item is absent.
     */
    public double getItemMean(long item) {
        int idx = itemIndex.tryGetIndex(item);
        return idx >= 0 ? itemOffsets[idx] + globalMean : Double.NaN;
    }

    /**
     * Get the item's average offset from global mean.
     * @param item The item.
     * @return The item's average offset from the global mean rating, or 0 if the item is missing
     */
    public double getItemOffset(long item) {
        int idx = itemIndex.tryGetIndex(item);
        return idx >= 0 ? itemOffsets[idx] : 0;
    }

    /**
     * Get the number of ratings for the item.
     */
    public int getItemRatingCount(long item) {
        int idx = itemIndex.tryGetIndex(item);
        return idx >= 0 ? itemCounts[idx] : 0;
    }

    public Long2DoubleSortedArrayMap getItemOffets() {
        return Long2DoubleSortedArrayMap.wrap(itemIndex, itemOffsets);
    }
}
