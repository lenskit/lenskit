package org.lenskit.knn.item;

import org.lenskit.results.AbstractResult;

/**
 * The result type for item-item collaborative filtering.
 */
public class ItemItemResult extends AbstractResult {
    private final int neighborhoodSize;

    /**
     * Construct a new item-item CF result.
     * @param item The item ID.
     * @param score The score.
     * @param nnbrs The neighborhood size.
     */
    public ItemItemResult(long item, double score, int nnbrs) {
        super(item, score);
        neighborhoodSize = nnbrs;
    }

    /**
     * Get the number of neighbors used to score this item.
     * @return The number of neighbors used to score this item.
     */
    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    @Override
    public int hashCode() {
        return startHashCode().append(neighborhoodSize).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemItemResult) {
            ItemItemResult or = (ItemItemResult) obj;
            return startEquality(or).append(neighborhoodSize, or.neighborhoodSize).isEquals();
        } else {
            return false;
        }
    }
}
