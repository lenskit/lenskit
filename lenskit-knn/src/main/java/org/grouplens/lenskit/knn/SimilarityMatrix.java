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
package org.grouplens.lenskit.knn;

import org.grouplens.lenskit.util.IndexedItemScore;


/**
 * Interface for similarity matrices for collaborative filtering.
 *
 * This interface uses numeric indexes for everything. The indexes are expected
 * to be contiguous starting with 0. Similarity matrices will usually have a
 * fixed size that cannot be exceeded.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface SimilarityMatrix {
    /**
     * The number of entities for which this matrix has similarities.
     *
     * @return
     */
    public int size();

    /**
     * Retrieve the neighbors for an item.
     *
     * @param i
     *            All neighbors for item <tt>i</tt>. This is all similarity
     *            scores for which <tt>i</tt> was passed as the first argument
     *            to {@link #put(int, int, double)}.
     * @return A map of neighbors to similarity scores.
     * @throws IndexOutOfBoundsException
     *             if the index is invalid.
     */
    public Iterable<IndexedItemScore> getNeighbors(int i);
}
