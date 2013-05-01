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

import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Math.sqrt;

/**
 * A real vector.  This is a read-only view of a vector of doubles.  It is backed by an array,
 * and contains values associated with indices [0,{@link #size()}).
 *
 * @compat Experimental — this interface may change in future versions of LensKit.
 */
public abstract class Vec implements Serializable {
    private static final long serialVersionUID = 1L;

    final double[] data;

    private transient volatile Double norm;
    private transient volatile Double sum;

    /**
     * Construct a vector from a backing array. The array is not copied.
     * @param d The backing array.
     */
    Vec(double[] d) {
        data = d;
    }

    /**
     * Clear cached values (sum, norm, etc.). This must be called by mutable subclasses whenever
     * they modify the vector to invalidate the cached sums.
     */
    void clearCaches() {
        norm = null;
        sum = null;
    }

    /**
     * Get the value from the vector at the specified position.
     * @param i The index into the vector.
     * @return The value at index {@code i}.
     * @throws IllegalArgumentException if {@code i} is not in the range [0,{@link #size()}).
     */
    public final double get(int i) {
        Preconditions.checkElementIndex(i, data.length);
        return data[i];
    }

    /**
     * Get the dimension of this vector (the number of elements).
     * @return The number of elements in the vector.
     */
    public final int size() {
        return data.length;
    }

    /**
     * Get the L2 (Euclidean) norm of this vector.
     * @return The Euclidean length of the vector.
     */
    public final double norm() {
        if (norm == null) {
            double ssq = 0;
            for (double v : data) {
                ssq += v * v;
            }
            norm = sqrt(ssq);
        }
        return norm;
    }

    /**
     * Get the sum of this vector.
     * @return The sum of the elements of the vector.
     */
    public final double sum() {
        if (sum == null) {
            double s = 0;
            for (double v : data) {
                s += v;
            }
            sum = s;
        }
        return sum;
    }

    /**
     * Get the mean of this vector.
     *
     * @return The mean of the elements of the vector (or {@link Double#NaN} if the vector is
     *         empty).
     */
    public final double mean() {
        return sum() / size();
    }

    /**
     * Compute the dot product of this vector with another.
     * @param other The other vector.
     * @return The dot product of this vector and {@code other}.
     * @throws IllegalArgumentException if {@code other.size() != this.size()}.
     */
    public final double dot(Vec other) {
        final int sz = data.length;
        Preconditions.checkArgument(sz == other.size(), "incompatible vector dimensions");
        double s = 0;
        for (int i = 0; i < sz; i++) {
            s += data[i] * other.data[i];
        }
        return s;
    }

    /**
     * Get an immutable vector with this vector's contents.  If the vector is already immutable,
     * it is not changed.
     * @return The immutable vector.
     */
    public ImmutableVec immutable() {
        return ImmutableVec.make(data);
    }

    /**
     * Get a mutable copy of this vector.
     * @return A mutable copy of this vector.
     */
    public MutableVec mutableCopy() {
        return MutableVec.wrap(Arrays.copyOf(data, data.length));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Vec) {
            return Arrays.equals(data, ((Vec) o).data);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
