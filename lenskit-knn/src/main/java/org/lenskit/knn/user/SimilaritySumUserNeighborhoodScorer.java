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

import org.lenskit.inject.Shareable;
import org.lenskit.knn.MinNeighbors;

import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * Score an item using the weighted average of neighbor ratings.
 */
@Immutable
@Shareable
public class SimilaritySumUserNeighborhoodScorer implements UserNeighborhoodScorer, Serializable {
    private static final long serialVersionUID = 1L;

    private final int minimumNeighbors;

    /**
     * Construct a weighted average scorer.
     * @param min The minimum neighbors.
     */
    @Inject
    public SimilaritySumUserNeighborhoodScorer(@MinNeighbors int min) {
        minimumNeighbors = min;
    }

    @Override
    @Nullable
    public UserUserResult score(long item, List<Neighbor> neighbors) {
        if (neighbors.size() < minimumNeighbors) {
            return null;
        }
        double weight = 0;
        for (Neighbor n: neighbors) {
            assert n.vector.get(item) > 0;
            weight += Math.abs(n.similarity);
        }
        if (weight > 0) {
            return UserUserResult.newBuilder()
                                 .setItemId(item)
                                 .setScore(weight)
                                 .setTotalWeight(weight)
                                 .setNeighborhoodSize(neighbors.size())
                                 .build();
        } else {
            return null;
        }
    }
}
