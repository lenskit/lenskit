/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.vectors.SparseVector;

import java.io.Serializable;

import static java.lang.Math.abs;

/**
 * Neighborhood scorer that computes the weighted average of neighbor scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class WeightedAverageNeighborhoodScorer implements NeighborhoodScorer, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Symbol NEIGHBORHOOD_WEIGHT_SYMBOL =
            Symbol.of("org.grouplens.lenskit.knn.item.neighborhoodWeight");

    @Override
    public ScoredId score(long item, Iterable<ScoredId> neighbors, SparseVector scores) {
        double sum = 0;
        double weight = 0;
        int n = 0;
        for (ScoredId id: CollectionUtils.fast(neighbors)) {
            long oi = id.getId();
            double sim = id.getScore();
            weight += abs(sim);
            sum += sim * scores.get(oi);
            n += 1;
        }
        if (weight > 0) {
            ScoredIdBuilder builder = ScoredIds.newBuilder();
            return builder.setId(item)
                          .setScore(sum/weight)
                          .addChannel(NEIGHBORHOOD_WEIGHT_SYMBOL,weight)
                          .build();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "[NeighborhoodScorer: WeightedAverage]";
    }
}
