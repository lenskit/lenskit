/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
