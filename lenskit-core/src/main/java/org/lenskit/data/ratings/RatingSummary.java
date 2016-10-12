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
