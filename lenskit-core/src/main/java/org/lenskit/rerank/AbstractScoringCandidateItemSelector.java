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
 *
 * @author Daniel Kluver
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

        if (bestResult != null) {
            bestResult = new RerankingRescoredResult(bestResult.getId(), bestScore, bestResult.getScore());
        }
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
