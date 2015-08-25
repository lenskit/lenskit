package org.lenskit.knn.user;

/**
 * Builder for user-user results.
 */
class ResultBuilder {
    private long itemId;
    private double rawScore;
    private double score;
    private int neighborhoodSize;
    private double totalWeight;

    public long getItemId() {
        return itemId;
    }

    public ResultBuilder setItemId(long itemId) {
        this.itemId = itemId;
        return this;
    }

    public double getRawScore() {
        return rawScore;
    }

    public ResultBuilder setRawScore(double rawScore) {
        this.rawScore = rawScore;
        return this;
    }

    public double getScore() {
        return score;
    }

    public ResultBuilder setScore(double score) {
        this.score = score;
        return this;
    }

    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public ResultBuilder setNeighborhoodSize(int neighborhoodSize) {
        this.neighborhoodSize = neighborhoodSize;
        return this;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public ResultBuilder setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
        return this;
    }

    public UserUserResult build() {
        return new UserUserResult(itemId, score, neighborhoodSize, totalWeight);
    }
}
