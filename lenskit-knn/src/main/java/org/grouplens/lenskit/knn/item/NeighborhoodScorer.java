/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
import org.grouplens.lenskit.vector.SparseVector;

/**
 * Compute scores from neighborhoods and score vectors.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface NeighborhoodScorer {
    /**
     * Compute a score based on similar neighbors and their corresponding
     * scores.
     *
     * @param neighbors A list of neighbors with similarity measures.
     * @param scores A vector of item scores. It should contain a score for
     *        every item in <var>neighbors</var>.
     * @return An accumulated score from the neighbors.
     */
    double score(ScoredLongList neighbors, SparseVector scores);
}
