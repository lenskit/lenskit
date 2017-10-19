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
package org.lenskit.knn.item;

import org.lenskit.results.AbstractResult;

/**
 * The result type for item-item collaborative filtering.
 */
public class ItemItemResult extends AbstractResult {
    private final int neighborhoodSize;
    private final double neighborWeight;

    /**
     * Construct a new item-item CF result.
     * @param item The item ID.
     * @param score The score.
     * @param nnbrs The neighborhood size.
     */
    public ItemItemResult(long item, double score, int nnbrs, double weight) {
        super(item, score);
        neighborhoodSize = nnbrs;
        neighborWeight = weight;
    }

    /**
     * Get the number of neighbors used to score this item.
     * @return The number of neighbors used to score this item.
     */
    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    /**
     * Get the total weight of the neighborhood.
     * @return The total weight of the neighborhood.
     */
    public double getNeighborWeight() {
        return neighborWeight;
    }

    /**
     * Rescore this item-item result.
     * @param score The new score.
     * @return The rescored result.
     */
    ItemItemResult rescore(double score) {
        return new ItemItemResult(getId(), score, getNeighborhoodSize(), getNeighborWeight());
    }

    @Override
    public String toString() {
        return "ItemItemResult{" +
                "id=" + getId() +
                ", score=" + getScore() +
                ", nnbrs=" + neighborhoodSize +
                ", weight=" + neighborWeight +
                '}';
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
