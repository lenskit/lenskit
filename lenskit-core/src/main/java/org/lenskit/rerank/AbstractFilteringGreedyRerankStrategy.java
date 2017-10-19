/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.rerank;

import org.lenskit.api.Result;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Abstract class designed to make implementation of a GreedyRerankStrategy easier.
 * Implements a method of selecting the first item that satisfies a constraint method.
 * Has abstract methods for the method of choosing how many items to consider before returning the best candidate.
 *
 * If you want items chosen based on optimizing some objective metric (or both an objective and a constraint)
 * see: {@link AbstractScoringGreedyRerankStrategy}.
 *
 * @author Daniel Kluver
 */
public abstract class AbstractFilteringGreedyRerankStrategy implements GreedyRerankStrategy {

    @Nullable
    @Override
    public Result nextItem(long userId, int n, List<? extends Result> items, List<? extends Result> candidates) {
        int numToInspect = computeNumToInspect(n, items.size(), candidates.size());
        Result bestResult = null;
        double bestScore = 0;
        for (int i = 0; i < numToInspect; i++) {
            Result candidate = candidates.get(i);
            if (satisfiesConstraint(userId, n, items, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Tests if adding the candidate item to the list of recommended items would satisfy a constraint.
     * @param userId the id of the user to recommend for
     * @param n the number of recommended items requested
     * @param items the list of items already chosen for recommendation
     * @param candidate the candidate item to recommend
     * @return true if adding the candidate to the recommendation would not violate the constraint.
     */
    protected abstract boolean satisfiesConstraint(long userId, int n, List<? extends Result> items, Result candidate);

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
