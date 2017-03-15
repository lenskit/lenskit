package org.lenskit.rerank;

import org.lenskit.api.Result;

import javax.annotation.Nullable;
import java.util.List;

//TODO: REVIEW: I'm not in love with this class name.
/**
 * Abstract class designed to make implementation of a CandidateItemSelector easier.
 * Implements a method of selecting the maximum scoring item that satisfies a constraint
 * Has abstract methods for the method of choosing how many items to consider before returning the best candidate.
 * note - While this class does have code for hard constraints, it is not a good option for implementing only a hard
 * constraint, in those cases an {@link AbstractFilteringCandidateItemSelector should be used.
 */
public abstract class AbstractScoringCandidateItemSelector implements CandidateItemSelector {

    @Nullable
    @Override
    public Result nextItem(long userId, int n, List<? extends Result> items, List<? extends Result> candidates) {
        int numToInspect = computeNumToInspect(n, items.size(), candidates.size());
        Result bestResult = null;
        double bestScore = 0;
        for (int i = 0; i < numToInspect; i++) {
            Result candidate = candidates.get(i);
            if (satisfiesConstraint(userId, n, items, candidate)) {
                double candidateScore = scoreCandidate(userId, n, items, candidate);
                if (bestResult == null || candidateScore > bestScore) {
                    bestResult = candidate;
                    bestScore = candidateScore;
                }
            }
        }
        // TODO: return a new Result type
        return bestResult;
    }

    /**
     * Computes an objective metric score for adding a given candidate item to a list of recommended items.
     * This method will only be called on items that satisfy the constraint.
     * @param userId the id of the user to recommend for
     * @param n the number of recommended items requested
     * @param items the list of items already chosen for recommendation
     * @param candidate the candidate item to recommend
     * @return a score used to chose which item to recommend, with larger values considered better.
     */
    protected abstract double scoreCandidate(long userId, int n, List<? extends Result> items, Result candidate);

    /**
     * Tests if adding the candidate item to the list of recommended items would satisfy a constraint.
     *
     * The default implementation returns true, effectively disabling the constraint by default.
     *
     * @param userId the id of the user to recommend for
     * @param n the number of recommended items requested
     * @param items the list of items already chosen for recommendation
     * @param candidate the candidate item to recommend
     * @return true if adding the candidate to the recommendation would not violate the constraint.
     */
    protected boolean satisfiesConstraint(long userId, int n, List<? extends Result> items, Result candidate) {
        return true;
    }

    /**
     * A method to compute how many of the items in the list to inspect before returning the best item found so far.
     * The default implementation searches the whole list. Override this method if you want to only search some prefix
     * of the ranking order for an optimal item.
     * @param numRequested the number of items requested for recommendation
     * @param numSelected the number of items which have already been selected
     * @param numCandidates the number of items which can still be selected from
     * @return the number of items to check before returning the best seen item.
     */
    protected int computeNumToInspect(int numRequested, int numSelected, int numCandidates) {
        return numCandidates;
    }
}
