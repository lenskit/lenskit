/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute scores from neighborhoods and score vectors.
 *
 * <p/>
 * This interface encapsulates aggregating user scores and item similarities into a final
 * score. The neighborhood is pre-filtered to only contain items for which
 * scores are available, and truncated to the neighborhood size, so all functions
 * implementing this interface need to do is accumulate scores.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface NeighborhoodScorer {
    /**
     * Compute a score based on similar neighbors and their corresponding
     * scores.
     *
     * @param neighbors A list of neighbors with similarity measures.
     * @param scores    A vector of item scores. It should contain a score for
     *                  every item in {@var neighbors}.
     * @return An accumulated score from the neighbors, or {@link Double#NaN} if
     *         no score could be computed.
     */
    double score(ScoredLongList neighbors, SparseVector scores);
}
