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

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.vector.ImmutableSparseVector;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.params.meta.DefaultClass;

/**
 * Normalize vectors by applying a reversible transformation with respect to
 * a reference vector.  The reference vector is used to compute the normalization,
 * and it is applied to the target vector; this allows e.g. the user's average
 * rating to be subtracted from a set of ratings.
 * 
 * @param <V> The type of reference vectors.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@DefaultClass(IdentityVectorNormalizer.class)
public interface VectorNormalizer<V extends ImmutableSparseVector> {

    /**
     * Normalize a vector in-place with a reference vector.
     * 
     * <p>
     * To understand the relationship of <var>reference</var> and
     * <var>vector</var>, consider wanting to subtract the user's mean rating
     * from a set of ratings. To do that, the user's rating vector is
     * <var>reference</var>, and the vector of ratings to be adjusted is
     * <var>vector</var>.
     * 
     * <p>
     * This method is equivalent to
     * <code>makeTransformation(reference).apply(target)</code).
     * 
     * @param reference The reference used to compute whatever transformation is
     *            needed (e.g. the mean value).
     * @param target The vector to normalize. If <tt>null</tt>, a new mutable
     *            copy of <var>reference</var> is created.
     * @return <var>target</var>, or a normalized mutable copy of
     *         <var>reference</var> if <var>target</var> is <tt>null</tt>.
     */
    MutableSparseVector normalize(V reference, @Nullable MutableSparseVector target);

    /**
     * Create a vector transformation that normalizes and denormalizes vectors
     * with respect to the specified entity. This allows transformations to be
     * applied multiple times to different vectors and also unapplied.
     * <p>
     * If the reference vector is empty, the returned transformation should be
     * the identity transform. Results are undefined if the reference vector is
     * not complete or contains NaN values.
     * 
     * @param reference The reference vector.
     * @return A transformation built from the reference vector.
     */
	VectorTransformation makeTransformation(V reference);

}