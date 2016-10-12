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
