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
 * Immutable {@link Vector}.  This vector cannot be modified (by anyone) and is thread-safe.
 *
 * @compat Experimental — this interface may change in future versions of LensKit.
 */
@Immutable
public class ImmutableVector extends Vector {
    ImmutableVector(double[] v) {
        super(v);
    }

    /**
     * Create a new vector from data in an array.  The array is copied for safety.
     *
     * @param data The data array.
     * @param length The number of elements to use, starting from the first.
     * @return A vector containing the data in {@code data}.
     */
    public static ImmutableVector make(double[] data, int length) {
        Preconditions.checkArgument(data.length >= length, "length mismatch");
        return new ImmutableVector(Arrays.copyOf(data, length));
    }

    /**
     * Create a new vector from data in an array.  The array is copied for safety.
     *
     * @param data The data array.
     * @return A vector containing the data in {@code data}.
     */
    public static ImmutableVector make(double[] data) {
        return make(data, data.length);
    }

    @Override
    public ImmutableVector immutable() {
        return this;
    }
}
