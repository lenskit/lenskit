package org.lenskit.rerank;

import org.lenskit.results.AbstractResult;

/**
 * Result class for {@link GreedyRerankingItemRecommender}
 *
 * @author Daniel Kluver
 */
public class GreedyRerankingResult extends AbstractResult {

    private final int rank;
    private final double greedyScore;

    GreedyRerankingResult(long id, double score, int rank, double greedyScore) {
        super(id, score);
        this.rank = rank;
        this.greedyScore = greedyScore;
    }

    public int getRank() {
        return rank;
    }

    public double getGreedyScore() {
        return greedyScore;
    }
}
