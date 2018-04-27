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
package org.lenskit.hybrid;

import org.lenskit.api.Result;
import org.lenskit.results.AbstractResult;

/**
 * Result type returned by {@link RankBlendingItemRecommender}.
 */
public class RankBlendResult extends AbstractResult {
    private final Result left, right;
    private final int leftRank, rightRank;

    RankBlendResult(long id, double score, Result lr, int lrank, Result rr, int rrank) {
        super(id, score);
        left = lr;
        leftRank = lrank;
        right = rr;
        rightRank = rrank;
    }

    /**
     * Get the left result.
     * @return The left result.
     */
    public Result getLeft() {
        return left;
    }

    /**
     * Get the left rank.
     * @return The left rank.
     */
    public int getLeftRank() {
        return leftRank;
    }

    /**
     * Get the right result.
     * @return The right result.
     */
    public Result getRight() {
        return right;
    }

    /**
     * Get the right rank
     * @return The right rank
     */
    public int getRightRank() {
        return rightRank;
    }
}
