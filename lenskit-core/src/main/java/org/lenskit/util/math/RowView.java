/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
