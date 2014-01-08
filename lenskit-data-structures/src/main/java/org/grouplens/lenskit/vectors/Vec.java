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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import static java.lang.Math.sqrt;

/**
 * A real vector.  This is a read-only view of a vector of doubles.  It is backed by an array,
 * and contains values associated with indices [0,{@link #size()}).
 *
 * @since 1.3
 * @compat Public
 */
public abstract class Vec implements Serializable {
    private static final long serialVersionUID = 1L;

    final double[] data;
    final int offset;
    final int size;
    final int stride;
    final int dataBound;

    /**
     * Construct a vector from a backing array. The array is not copied.
     * @param d The backing array.
     * @param off The offset into the array.
     * @param sz The size of the vector.
     * @param str The stride of the vector (distance between elements).
     */
    @SuppressWarnings("PMD.ArrayIsStoredDirectly") // that's the point
    Vec(double[] d, int off, int sz, int str) {
        assert off >= 0;
        assert str >= 1;
        assert sz == 0 || off + (sz - 1) * str < d.length;
        data = d;
        offset = off;
        size = sz;
        stride = str;
        dataBound = offset + size * stride;
    }

    /**
     * Convert a vector index into an array index.
     * @param i The index in the vector.
     * @return The index in the underlying array.
     */
    int arrayIndex(int i) {
        Preconditions.checkElementIndex(i, size);
        return offset + i * stride;
    }

    /**
     * Get the value from the vector at the specified position.
     * @param i The index into the vector.
     * @return The value at index {@code i}.
     * @throws IndexOutOfBoundsException if {@code i} is not in the range [0,{@link #size()}).
     */
    public final double get(int i) {
        return data[arrayIndex(i)];
    }

    /**
     * Get the dimension of this vector (the number of elements).
     * @return The number of elements in the vector.
     */
    public final int size() {
        return size;
    }

    /**
     * Get the L2 (Euclidean) norm of this vector.
     *
     * @return The Euclidean length of the vector.
     */
    public double norm() {
        double ssq = 0;
        for (int i = offset; i < dataBound; i += stride) {
            double v = data[i];
            ssq += v * v;
        }
        return sqrt(ssq);
    }

    /**
     * Get the sum of this vector.
     *
     * @return The sum of the elements of the vector.
     */
    public double sum() {
        double s = 0;
        for (int i = offset; i < dataBound; i += stride) {
            double v = data[i];
            s += v;
        }
        return s;
    }

    /**
     * Get the mean of this vector.
     *
     * @return The mean of the elements of the vector (or {@link Double#NaN} if the vector is
     *         empty).
     */
    public double mean() {
        return sum() / size();
    }

    /**
     * Compute the dot product of this vector with another.
     * @param other The other vector.
     * @return The dot product of this vector and {@code other}.
     * @throws IllegalArgumentException if {@code other.size() != this.size()}.
     */
    public final double dot(Vec other) {
        Preconditions.checkArgument(size == other.size(), "incompatible vector dimensions");
        double s = 0;
        for (int i = offset, j = other.offset; i < dataBound;
             i += stride, j += other.stride) {
            assert j < other.dataBound;
            s += data[i] * other.data[j];
        }
        return s;
    }

    /**
     * Get the largest element of the vector.
     *
     * @return The index of largest element of the vector.
     */
    public int largestDimension() {
        double largest = data[0];
        int index = 0;
        for(int i = 1; i < data.length; i++) {
            if(data[i] > largest) {
                largest = data[i];
                index = i;
            }
        }
        return index;
    }
    
    /**
     * Make a copy of this vector as an array.
     * @return An array containing the contents of this vector.
     */
    public double[] toArray() {
        double[] newData = new double[size];
        if (stride == 1) {
            System.arraycopy(data, offset, newData, 0, size);
        } else {
            for (int i = 0, j = offset; i < size; i++, j += stride) {
                assert j < dataBound;
                newData[i] = data[j];
            }
        }
        return newData;
    }


    /**
     * Get an immutable vector with this vector's contents.  If the vector is already immutable,
     * it is not changed.
     * @return The immutable vector.
     */
    public ImmutableVec immutable() {
        return new ImmutableVec(toArray(), 0, size, 1);
    }

    /**
     * Get a mutable copy of this vector.
     * @return A mutable copy of this vector.
     */
    public MutableVec mutableCopy() {
        return MutableVec.wrap(toArray());
    }

    /**
     * Create a new strided subvector of this vector.  The returned vector is a view of the original
     * vector; if it is modifiable, modifying it will propagate to the original (and all views).
     *
     * @param offset The offset of the subvector.
     * @param size The size (dimension) of the desired vector.
     * @param stride The stride of the vector.
     * @return A vector representing a potentially-strided view of this vector.
     * @throws IllegalArgumentException if the specified vector does not fit in this one.
     */
    public abstract Vec subVector(int offset, int size, int stride);

    /**
     * Create a new subvector of this vector.  The returned vector is a view of the original vector;
     * if it is modifiable, modifying it will propagate to the original (and all views).
     *
     * @param offset The offset of the subvector.
     * @param size The size (dimension) of this vector.
     * @return A vector representing a view of this vector.
     */
    public abstract Vec subVector(int offset, int size);

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Vec) {
            Vec ov = (Vec) o;
            EqualsBuilder eqb = new EqualsBuilder();
            eqb.append(size, ov.size);
            for (int i = 0; i < size && eqb.isEquals(); i++) {
                eqb.append(get(i), ov.get(i));
            }
            return eqb.isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        for (int i = offset; i < dataBound; i += stride) {
            hcb.append(data[i]);
        }
        return hcb.toHashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
          .append("<");
        for (int i = offset; i < dataBound; i += stride) {
            if (i > offset) {
                sb.append(",");
            }
            sb.append(data[i]);
        }
        return sb.append(">").toString();
    }
}
