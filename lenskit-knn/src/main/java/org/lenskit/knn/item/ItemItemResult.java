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
package org.lenskit.knn.item;

import org.lenskit.results.AbstractResult;

/**
 * The result type for item-item collaborative filtering.
 */
public class ItemItemResult extends AbstractResult {
    private final int neighborhoodSize;

    /**
     * Construct a new item-item CF result.
     * @param item The item ID.
     * @param score The score.
     * @param nnbrs The neighborhood size.
     */
    public ItemItemResult(long item, double score, int nnbrs) {
        super(item, score);
        neighborhoodSize = nnbrs;
    }

    /**
     * Get the number of neighbors used to score this item.
     * @return The number of neighbors used to score this item.
     */
    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    @Override
    public int hashCode() {
        return startHashCode().append(neighborhoodSize).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemItemResult) {
            ItemItemResult or = (ItemItemResult) obj;
            return startEquality(or).append(neighborhoodSize, or.neighborhoodSize).isEquals();
        } else {
            return false;
        }
    }
}
