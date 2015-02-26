/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.transform.normalize;

import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Reversible in-place vector transformations.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface VectorTransformation {
    /**
     * Apply the vector transformation in-place to a vector.
     *
     * @param vector The vector to transform.
     * @return <var>vector</var> (for chaining).
     */
    MutableSparseVector apply(MutableSparseVector vector);

    /**
     * Unapply the vector transformation in-place on a transformed vector. In
     * some cases, the unapplication may supply values for key domain members
     * that do not have values.
     *
     * @param vector The vector to transform.
     * @return <var>vector</var> (for chaining).
     */
    MutableSparseVector unapply(MutableSparseVector vector);
}
