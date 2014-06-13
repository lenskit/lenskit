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
package org.grouplens.lenskit.vectors.similarity;


import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute the similarity between sparse vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(CosineVectorSimilarity.class)
public interface VectorSimilarity {
    /**
     * Compute the similarity between two vectors.
     *
     * @param vec1 The left vector to compare.
     * @param vec2 The right vector to compare.
     * @return The similarity, in the range [-1,1].
     */
    double similarity(SparseVector vec1, SparseVector vec2);

    /**
     * Query whether this similarity function is sparse (returns 0 for vectors with
     * disjoint key sets).
     *
     * @return {@code true} iff {@link #similarity(SparseVector, SparseVector)} will always return
     *         true when applied to two vectors with no keys in common.
     */
    boolean isSparse();

    /**
     * Query whether this similarity function is symmetric. Symmetric similarity functions
     * return the same result when called on (A,B) and (B,A).
     *
     * @return {@code true} if the function is symmetric.
     */
    boolean isSymmetric();
}
