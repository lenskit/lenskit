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
package org.grouplens.lenskit.norm;

import java.io.Serializable;

import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Interface for rating vector normalizing user rating vectors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
// FIXME: discuss if this is the appropriate level for normalizer serialization
// In some ways it feels like BaselinePredictor (which extends Serializable directly),
// but it might be that some normalizers want to depend on the Dao directly,
// in which case being Serializable doesn't make sense
// FIXME: Similarity also extends Serializable for convenience reasons, too
//   SimilarityMatrix does not, because there's a chance that it would want to be Externalizable
public interface UserRatingVectorNormalizer extends Serializable {
    /**
     * Normalize a rating vector in-place.
     * @param userId The user's ID.
     * @param ratings The user's rating vector.
     * @param vector The rating vector to normalize. This can be the same object
     * as <var>ratings</var>.
     */
    void normalize(long userId, SparseVector ratings, MutableSparseVector vector);
    
    /**
     * Normalize the user's rating vector in-place.
     * @param userId The user's ID.
     * @param ratings The user's rating vector. It is modified in-place to be
     * normalized.
     */
    void normalize(long userId, MutableSparseVector ratings);
    
    /**
     * Create a vector transformation that normalizes and denormalizes vectors with
     * respect to the specified user. This allows transformations to be applied
     * multiple times and also unapplied.
     * @param userId The user's ID.
     * @param ratings The user's rating vector.
     */
    VectorTransformation makeTransformation(long userId, SparseVector ratings);
}
