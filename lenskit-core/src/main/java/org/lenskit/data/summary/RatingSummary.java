package org.lenskit.data.summary;

import org.grouplens.lenskit.core.Shareable;
import org.lenskit.util.keys.KeyedObject;
import org.lenskit.util.keys.KeyedObjectMap;

import java.io.Serializable;

/**
 * A summary of the ratings data.
 */
@Shareable
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
        private final double meanRating;
        private final int ratingCount;

        public ItemSummary(long item, double mean, int count) {
            id = item;
            meanRating = mean;
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
            return meanRating;
        }

        public int getRatingCount() {
            return ratingCount;
        }
    }
}
