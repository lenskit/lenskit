/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Immutable {@link Vec}.  This vector cannot be modified (by anyone) and is thread-safe.  You can
 * create one using {@link #create(double[])} or {@link #immutable()}.
 *
 * @since 1.3
 * @deprecated Use vectorz instead.
 */
@Deprecated
@Immutable
public final class ImmutableVec extends Vec {
    private static final long serialVersionUID = 1L;

    private transient volatile Double sum;
    private transient volatile Double norm;

    /**
     * Construct a new immutable vector.
     * @param v The vector's contents.
     */
    private ImmutableVec(double[] v) {
        super(v);
    }

    /**
     * Create a new immutable vector backed by an array.
     *
     * @param data The data array.
     * @return A new vector wrapping the specified array. It is the caller's responsibility to make
     *         sure that this cannot be modified anymore!
     */
    static ImmutableVec wrap(double[] data) {
        return new ImmutableVec(data);
    }

    /**
     * Create a new vector from data in an array.  The array is copied for safety.
     *
     * @param data The data array.
     * @return A vector containing the data in {@code data}.
     */
    public static ImmutableVec create(double[] data) {
        return new ImmutableVec(Arrays.copyOf(data, data.length));
    }

    @Override
    public ImmutableVec immutable() {
        return this;
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
