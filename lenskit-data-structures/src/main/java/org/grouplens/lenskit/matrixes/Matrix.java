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

import org.grouplens.lenskit.vectors.Vec;

import java.util.List;

/**
 * Two-dimensional matrix API (read-only interface).
 *
 * <p>A matrix is a two-dimensional grid of double values.  All indices are 0-based.</p>
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface Matrix {
    /**
     * Get the number of rows in this matrix.
     * @return The number of rows in the matrix.
     */
    int getRowCount();

    /**
     * Get the number of columns in this matrix.
     * @return The number of columns in the matrix.
     */
    int getColumnCount();

    /**
     * Get the value at a particular row and column index.
     * @param r The row index (0-based).
     * @param c The column index (0-based).
     * @return The value at (r,c).
     * @throws IndexOutOfBoundsException if either {@code r} or {@code c} is out of bounds.
     */
    double get(int r, int c);

    /**
     * Get a particular row as a vector.
     * @param r The row to retrieve.
     * @return A view of the row as a vector.
     */
    Vec row(int r);

    /**
     * Get a particular column as a vector.
     * @param c The column to retrieve.
     * @return A view of the column as a vector.
     */
    Vec column(int c);

    /**
     * Get an immutable version of this matrix.  If the matrix is mutable, a copy is created.
     * @return An immutable matrix with the same contents as this matrix.
     */
    ImmutableMatrix immutable();

    /**
     * Get a mutable copy of this matrix.
     * @return A new mutable matrix with a copy of this matrix's contents.
     */
    MutableMatrix mutableCopy();

    /**
     * Get the rows of this matrix as a list.
     * @return A list of the rows of this matrix.
     */
    List<Vec> rows();

    /**
     * Get the columns of this matrix as a list.
     * @return A list of the columns of this matrix.
     */
    List<Vec> columns();
}
