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
import org.grouplens.lenskit.vectors.ImmutableVec;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class RowMajorImmutableMatrix extends RowMajorMatrix implements ImmutableMatrix {
    private static final long serialVersionUID = 1L;
    private final ImmutableVec data;

    RowMajorImmutableMatrix(ImmutableVec data, int rowDim, int colDim) {
        super(rowDim, colDim);
        Preconditions.checkArgument(data.size() == rowDim * colDim,
                                    "dimension mismatch");
        this.data = data;
    }

    @Override
    public ImmutableVec row(int r) {
        Preconditions.checkPositionIndex(r, rowDim, "row");
        return data.subVector(r * colDim, colDim);
    }

    @Override
    public ImmutableVec column(int c) {
        Preconditions.checkPositionIndex(c, colDim, "column");
        return data.subVector(c, rowDim, colDim);
    }

    @Override
    public double get(int r, int c) {
        return data.get(addressToIndex(r, c));
    }

    @Override
    public ImmutableMatrix immutable() {
        return this;
    }

    @Override
    public MutableMatrix mutableCopy() {
        return new RowMajorMutableMatrix(data.mutableCopy(), rowDim, colDim);
    }
}
