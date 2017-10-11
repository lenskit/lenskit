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

/**
 * Builder for user-user results.
 */
class ResultBuilder {
    private long itemId;
    private double score;
    private int neighborhoodSize;
    private double totalWeight;

    public long getItemId() {
        return itemId;
    }

    public ResultBuilder setItemId(long itemId) {
        this.itemId = itemId;
        return this;
    }

    public double getScore() {
        return score;
    }

    public ResultBuilder setScore(double score) {
        this.score = score;
        return this;
    }

    public int getNeighborhoodSize() {
        return neighborhoodSize;
    }

    public ResultBuilder setNeighborhoodSize(int neighborhoodSize) {
        this.neighborhoodSize = neighborhoodSize;
        return this;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public ResultBuilder setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
        return this;
    }

    public UserUserResult build() {
        return new UserUserResult(itemId, score, neighborhoodSize, totalWeight);
    }
}
