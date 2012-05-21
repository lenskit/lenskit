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
package org.grouplens.lenskit.knn.user;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute the similarity between two users.
 * @author Michael Ekstrand
 * @since 0.11
 */
@DefaultImplementation(UserVectorSimilarity.class)
public interface UserSimilarity {
    /**
     * Compute the similarity between two users.
     * @param u1 The first user ID.
     * @param v1 The first user vector.
     * @param u2 The second user ID.
     * @param v2 The second user vector.
     * @return The similarity between the two users, in the range [0,1].
     */
    double similarity(long u1, SparseVector v1, long u2, SparseVector v2);

    /**
     * Query whether this similarity is sparse.
     * @return {@code true} if the similarity function is sparse.
     * @see org.grouplens.lenskit.knn.VectorSimilarity#isSparse()
     */
    boolean isSparse();

    /**
     * Query whether this similarity is symmetric.
     * @return {@code true} if the similarity function is symmetric.
     * @see org.grouplens.lenskit.knn.VectorSimilarity#isSymmetric()
     */
    boolean isSymmetric();
}
