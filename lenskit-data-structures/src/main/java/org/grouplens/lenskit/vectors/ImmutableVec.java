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
package org.grouplens.lenskit.vectors;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Immutable {@link Vec}.  This vector cannot be modified (by anyone) and is thread-safe.  You can
 * create one using {@link #create(double[])} or {@link #immutable()}.
 *
 * @since 1.3
 * @compat
 */
@Immutable
public final class ImmutableVec extends Vec {
    private static final long serialVersionUID = 1L;

    private transient volatile Double sum;
    private transient volatile Double norm;

    /**
     * Construct a new immutable vector.
     * @param v The vector's contents.
     */
    ImmutableVec(double[] v, int offset, int size, int stride) {
        super(v, offset, size, stride);
    }

    /**
     * Create a new vector from data in an array.  The array is copied for safety.
     *
     * @param data The data array.
     * @return A vector containing the data in {@code data}.
     */
    public static ImmutableVec create(double... data) {
        return new ImmutableVec(Arrays.copyOf(data, data.length), 0, data.length, 1);
    }

    @Override
    public ImmutableVec immutable() {
        return this;
    }

    @Override
    public ImmutableVec subVector(int offset, int size) {
        return subVector(offset, size, 1);
    }

    @Override
    public ImmutableVec subVector(int voff, int vsize, int vstride) {
        Preconditions.checkPositionIndex(voff, size, "offset");
        Preconditions.checkArgument(vstride >= 1, "stride is not positive");
        if (vsize > 0) {
            Preconditions.checkPositionIndex(voff + (vsize - 1) * vstride, size, "upper bound");
        }
        int noff = offset + voff * stride;
        int nstride = vstride * stride;
        return new ImmutableVec(data, noff, vsize, nstride);
    }

    @Override
    public double sum() {
        Double s = sum;
        if (s == null) {
            sum = s = super.sum();
        }
        return s;
    }

    @Override
    public double norm() {
        Double n = norm;
        if (n == null) {
            norm = n = super.norm();
        }
        return n;
    }
}
