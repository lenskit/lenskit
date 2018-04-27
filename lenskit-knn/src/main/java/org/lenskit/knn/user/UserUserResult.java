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
package org.lenskit.knn.user;

import org.lenskit.results.AbstractResult;

/**
 * Result for user-user CF.
 */
public final class UserUserResult extends AbstractResult {
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

    public ResultBuilder copyBuilder() {
        return newBuilder()
                .setItemId(getId())
                .setNeighborhoodSize(getNeighborhoodSize())
                .setTotalWeight(getTotalNeighborWeight())
                .setScore(getScore());
    }

    @Override
    public int hashCode() {
        return startHashCode().append(neighborhoodSize)
                              .append(neighborWeight)
                              .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserUserResult) {
            UserUserResult or = (UserUserResult) obj;
            return startEquality(or).append(neighborhoodSize, or.neighborhoodSize)
                                    .append(neighborWeight, or.neighborWeight)
                                    .isEquals();
        } else {
            return false;
        }
    }
}
