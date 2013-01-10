/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item.model;

/**
 * Interface for accumulators of item similarities going into
 * the item-item CF model.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface SimilarityMatrixAccumulator {

    /**
     * Store an entry in the similarity matrix.
     *
     * @param i   The matrix row (an item ID).
     * @param j   The matrix column (an item ID).
     * @param sim The similarity between items {@code j} and {@code i}. As documented in the
     *            {@link org.grouplens.lenskit.knn.item package docs}, this is \(s(j,i)\).
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void put(long i, long j, double sim);

    /**
     * Tells the accumulator that the row being constructed
     * has been completed, allowing the accumulator to process
     * the row as a whole if it so desires.
     *
     * @param rowId The long id of the row which has been completed.
     */
    public void completeRow(long rowId);

    /**
     * Moves the result matrix into a SimilarityMatrixModel.
     *
     * @return The resulting SimilarityMatrixModel.
     */
    public SimilarityMatrixModel build();

}
