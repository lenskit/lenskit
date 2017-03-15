package org.lenskit.rerank;

import org.lenskit.results.AbstractResult;

/**
 * A class to store the original score as well as the objective metric score returned by the
 * objective function algorithm.
 *
 * @see AbstractScoringCandidateItemSelector
 */
public class RerankingRescoredResult extends AbstractResult {
    private final double originalScore;

    public RerankingRescoredResult(long id, double score, double originalScore) {
        super(id, score);
        this.originalScore = originalScore;
    }

    public double getOriginalScore() {
        return originalScore;
    }
}
