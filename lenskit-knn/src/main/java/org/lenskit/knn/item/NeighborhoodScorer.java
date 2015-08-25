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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute scores from neighborhoods and score vectors.
 *
 * <p>
 * This interface encapsulates aggregating user scores and item similarities into a final
 * score. The neighborhood is pre-filtered to only contain items for which
 * scores are available, and truncated to the neighborhood size, so all functions
 * implementing this interface need to do is accumulate scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(WeightedAverageNeighborhoodScorer.class)
public interface NeighborhoodScorer {
    /**
     * Compute a score based on similar neighbors and their corresponding
     * scores.
     *
     *
     *
     * @param item
     * @param neighbors A vector of neighbors with similarity measures.
     * @param scores    A vector of item scores. It should contain a score for
     *                  every item in <var>neighbors</var>.
     * @return An accumulated score from the neighbors, or {@code null} if
     *         no score could be computed.
     */
    ItemItemResult score(long item, Long2DoubleMap neighbors, Long2DoubleMap scores);
}
