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
package org.grouplens.lenskit.matrixes;

import org.grouplens.lenskit.vectors.MutableVec;

/**
 * Mutable 2D matrix API.  Rows and column vectors returned by this matrix are mutable views
 * of the matrix; modifying them modifies the underlying matrix.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface MutableMatrix extends Matrix {
    /**
     * {@inheritDoc}
     * This returns a <em>mutable</em> view of the row.  Modifying the vector will modify the matrix.
     */
    MutableVec row(int r);

    /**
     * {@inheritDoc}
     * This returns a <em>mutable</em> view of the column.  Modifying the vector will modify the matrix.
     */
    MutableVec column(int c);

    /**
     * Set a particular index to a value.
     * @param r The row index.
     * @param c The column index.
     * @param v The value to set them to.
     * @throws IndexOutOfBoundsException if either {@code r} or {@code c} is out of bounds.
     */
    void set(int r, int c, double v);

    /**
     * Convert this matrix into an immutable matrix.  The matrix cannot be used after it is frozen.
     * @return An immutable matrix containing this matrix's contents, reusing the underlying storage.
     */
    ImmutableMatrix freeze();
}
