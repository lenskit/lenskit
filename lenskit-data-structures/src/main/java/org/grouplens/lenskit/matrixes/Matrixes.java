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

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.vectors.MutableVec;

/**
 * Utility methods for working with matrices.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Matrixes {
    private Matrixes() {}

    /**
     * Create a new, zeroed matrix with the specified number of rows and columns.
     * @param rows The number of rows.
     * @param cols The number of columns.
     * @return An {@code rows} by {@code cols} matrix filled with zeros.
     */
    public static MutableMatrix create(int rows, int cols) {
        Preconditions.checkArgument(rows >= 0, "row count is negative");
        Preconditions.checkArgument(cols >= 0, "column count is negative");
        return new RowMajorMutableMatrix(MutableVec.create(rows * cols),
                                         rows, cols);
    }
}
