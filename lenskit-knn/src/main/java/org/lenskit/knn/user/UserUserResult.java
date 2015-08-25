package org.lenskit.knn.user;

import org.lenskit.results.AbstractResult;

/**
 * Result for user-user CF.
 */
public class UserUserResult extends AbstractResult {
    private final int neighborhoodSize;
    private final double neighborWeight;

    UserUserResult(long item, double score, int nnbrs, double weight) {
        super(item, score);
        neighborhoodSize = nnbrs;
        neighborWeight = weight;
    }

    static ResultBuilder newBuilder() {
        return new ResultBuilder();
    }

    /**
     * Get the neighborhood size for this result.
     * @return The number of neighbors used to compute the result.
     */
    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    /**
     * Get the total neighbor weight for this result.
     * @return The total weight (similarity) of the neighbors.
     */
    public double getTotalNeighborWeight() {
        return neighborWeight;
    }
}
