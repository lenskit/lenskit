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
package org.grouplens.lenskit.knn.matrix;

import org.grouplens.lenskit.RecommenderComponentBuilder;

/**
 * Interface for building similarity matrices for collaborative filtering. It is
 * named Accumulator to dissociate it from the builders implementing
 * {@link RecommenderComponentBuilder}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface SimilarityMatrixAccumulator {
    /**
     * Store the similarity between two entities. Calls to this method are
     * thread-safe with each other. Only one similarity should be stored for
     * each ordered pair of entities, as the implementation may not de-duplicate
     * the matrix. {@link #build()} cannot be called concurrently with this
     * method.
     * 
     * @param i1 The ID of the first entity.
     * @param i2 The ID of the second entity.
     * @param sim The similarity score.
     */
    public void put(long i1, long i2, double sim);

    /**
     * Store a symmetric similarity.
     * @see #put(long, long, double)
     * @param i1 The ID of the first entity.
     * @param i2 The ID of the second entity
     * @param sim The similarity score
     */
    void putSymmetric(long i1, long i2, double sim);

    /**
     * Finish building the matrix and return it.  The builder can no longer be
     * used after this.  This method cannot be called concurrently with
     * {@link #put(long, long, double)}.
     * @return The finalized similarity matrix.
     */
    public SimilarityMatrix build();
}
