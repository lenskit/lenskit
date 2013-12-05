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
 * Mutable row-major implementation of a matrix.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class RowMajorMutableMatrix extends RowMajorMatrix implements MutableMatrix {
    private final MutableVec data;

    RowMajorMutableMatrix(MutableVec data, int rowDim, int colDim) {
        super(rowDim, colDim);
        Preconditions.checkArgument(data.size() == rowDim * colDim,
                                    "dimension mismatch");
        this.data = data;
    }

    @Override
    public MutableVec row(int r) {
        return data.subVector(r * colDim, colDim);
    }

    @Override
    public MutableVec column(int c) {
        return data.subVector(c, rowDim, colDim);
    }

    @Override
    public double get(int r, int c) {
        return data.get(addressToIndex(r, c));
    }

    @Override
    public void set(int r, int c, double v) {
        data.set(addressToIndex(r, c), v);
    }

    @Override
    public ImmutableMatrix freeze() {
        return new RowMajorImmutableMatrix(data.freeze(), rowDim, colDim);
    }

    @Override
    public ImmutableMatrix immutable() {
        return new RowMajorImmutableMatrix(data.immutable(), rowDim, colDim);
    }

    @Override
    public MutableMatrix mutableCopy() {
        return new RowMajorMutableMatrix(data.mutableCopy(), rowDim, colDim);
    }
}
