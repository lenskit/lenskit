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
package org.lenskit.data.summary;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.lenskit.util.keys.KeyedObject;
import org.lenskit.util.keys.KeyedObjectMap;

import java.io.Serializable;

/**
 * A summary of the ratings data.
 *
 * @since 3.0
 */
@Shareable
@DefaultProvider(RatingSummaryBuilder.class)
public class RatingSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double globalMean;
    private final KeyedObjectMap<ItemSummary> itemSummaries;

    /**
     * Construct a new rating summary.
     * @param mean The global mean rating.
     * @param items The item summaries.
     */
    RatingSummary(double mean, KeyedObjectMap<ItemSummary> items) {
        globalMean = mean;
        itemSummaries = items;
    }

    /**
     * Create a rating summary from an event DAO. This uses {@link FastRatingSummaryBuilder} and is therefore efficient
     * but does not handle unrate events.
     * @param dao The events.
     * @return The rating summary.
     */
    public static RatingSummary create(EventDAO dao) {
        return new FastRatingSummaryBuilder(dao).get();
    }

    public double getGlobalMean() {
        return globalMean;
    }

    /**
     * Get the mean rating for an item.
     * @param item The item.
     * @return The item's mean rating, or {@link Double#NaN} if the item is absent.
     */
    public double getItemMean(long item) {
        ItemSummary sum = itemSummaries.get(item);
        return sum != null ? sum.getMeanRating() : Double.NaN;
    }

    /**
     * Get the item's average offset from global mean.
     * @param item The item.
     * @return The item's average offset from the global mean rating, or 0 if the item is missing
     */
    public double getItemOffset(long item) {
        ItemSummary sum = itemSummaries.get(item);
        if (sum == null) {
            return 0;
        } else {
            return sum.getMeanRating() - globalMean;
        }
    }

    /**
     * Get the number of ratings for the item.
     */
    public int getItemRatingCount(long item) {
        ItemSummary sum = itemSummaries.get(item);
        return sum != null ? sum.getRatingCount() : 0;
    }

    static class ItemSummary implements Serializable, KeyedObject {
        private static final long serialVersionUID = 1L;

        private final long id;
        private final double sumOfRatings;
        private final int ratingCount;

        public ItemSummary(long item, double sum, int count) {
            id = item;
            sumOfRatings = sum;
            ratingCount = count;
        }

        public long getId() {
            return id;
        }

        @Override
        public long getKey() {
            return id;
        }

        public double getMeanRating() {
            return sumOfRatings;
        }

        public int getRatingCount() {
            return ratingCount;
        }
    }
}
