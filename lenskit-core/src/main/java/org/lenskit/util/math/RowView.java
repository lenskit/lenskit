/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.math;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * View class for a row of a matrix.
 */
class RowView extends RealVector {
    private final RealMatrix matrix;
    private final int row;
    private final int offset;
    private final int length;

    RowView(RealMatrix mat, int r) {
        this(mat, r, 0, mat.getColumnDimension());
    }
    RowView(RealMatrix mat, int r, int off, int len) {
        matrix = mat;
        row = r;
        offset = off;
        length = len;
    }

    @Override
    public int getDimension() {
        return length;
    }

    @Override
    public double getEntry(int index) throws OutOfRangeException {
        return matrix.getEntry(row, offset + index);
    }

    @Override
    public void setEntry(int index, double value) throws OutOfRangeException {
        throw new UnsupportedOperationException("read-only vector");
    }

    @Override
    public RealVector append(RealVector v) {
        return new ArrayRealVector(this).append(v);
    }

    @Override
    public RealVector append(double d) {
        return new ArrayRealVector(this).append(d);
    }

    @Override
    public RealVector getSubVector(int index, int n) throws NotPositiveException, OutOfRangeException {
        // FIXME Catch and throw errors
        return new RowView(matrix, row, offset + index, n);
    }

    @Override
    public void setSubVector(int index, RealVector v) throws OutOfRangeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNaN() {
        for (int i = 0; i < length; i++) {
            if (Double.isNaN(getEntry(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInfinite() {
        for (int i = 0; i < length; i++) {
            if (Double.isInfinite(getEntry(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RealVector copy() {
        return new ArrayRealVector(this);
    }

    @Override
    public RealVector ebeDivide(RealVector v) throws DimensionMismatchException {
        return new ArrayRealVector(this).ebeDivide(v);
    }

    @Override
    public RealVector ebeMultiply(RealVector v) throws DimensionMismatchException {
        return new ArrayRealVector(this).ebeMultiply(v);
    }
}
