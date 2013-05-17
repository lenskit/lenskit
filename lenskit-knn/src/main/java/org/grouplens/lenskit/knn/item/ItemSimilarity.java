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
package org.grouplens.lenskit.knn.item;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute the similarity between two items.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
@DefaultImplementation(ItemVectorSimilarity.class)
public interface ItemSimilarity {
    /**
     * Compute the similarity between two items.
     *
     * @param i1 The first item ID.
     * @param v1 The first item vector.
     * @param i2 The second item ID.
     * @param v2 The second item vector.
     * @return The similarity between the two items, in the range [0,1].
     */
    double similarity(long i1, SparseVector v1, long i2, SparseVector v2);

    /**
     * Query whether this similarity is sparse.
     *
     * @return {@code true} if the similarity function is sparse.
     * @see org.grouplens.lenskit.vectors.similarity.VectorSimilarity#isSparse()
     */
    boolean isSparse();

    /**
     * Query whether this similarity is symmetric.
     *
     * @return {@code true} if the similarity function is symmetric.
     * @see org.grouplens.lenskit.vectors.similarity.VectorSimilarity#isSymmetric()
     */
    boolean isSymmetric();
}
