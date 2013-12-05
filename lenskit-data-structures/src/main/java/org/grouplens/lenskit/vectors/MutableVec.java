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

import javax.annotation.Nullable;

/**
 * Mutable {@link Vec}.  This vector can be modified and is not
 * thread-safe.  Create one with {@link #create(int)} or {@link #wrap(double[])}.
 *
 * @since 1.3
 * @compat Public
 */
public final class MutableVec extends Vec {
    private static final long serialVersionUID = 2L;
    private final MutableVec rootVector;
    private boolean frozen = false;

    /**
     * Construct a new vector. The array is <b>not</b> copied.
     * @param v The backing array.
     * @param offset The offset into the array
     * @param size The size of the resulting vector
     * @param root The root/owning vector that this vector is a subview of.
     */
    private MutableVec(double[] v, int offset, int size, int stride,
                       @Nullable MutableVec root) {
        super(v, offset, size, stride);
        if (root == null) {
            rootVector = this;
        } else {
            rootVector = root;
        }
    }

    private void checkFrozen() {
        if (rootVector.frozen) {
            throw new IllegalStateException("vector is frozen");
        }
    }

    /**
     * Create a new mutable vector with the specified size.
     * @param dim The size of the new vector.
     */
    public static MutableVec create(int dim) {
        return new MutableVec(new double[dim], 0, dim, 1, null);
    }

    /**
     * Create a new vector wrapping an existing array.
     *
     * @param data The data array.
     * @return A vector backed by {@code data}.
     */
    public static MutableVec wrap(double[] data) {
        return new MutableVec(data, 0, data.length, 1, null);
    }

    /**
     * Set a value in this vector.
     * @param i The index.
     * @param v The value to set.
     * @return The old value at {@code i}.
     * @throws IllegalArgumentException if {@code i} is not a valid index in this vector.
     */
    public double set(int i, double v) {
        checkFrozen();
        int idx = arrayIndex(i);
        final double old = data[idx];
        data[idx] = v;
        return old;
    }

    /**
     * Add a value to an entry in this vector.
     *
     * @param i The index.
     * @param v The value to set.
     * @return The old value at {@code i}.
     * @throws IllegalArgumentException if {@code i} is not a valid index in this vector.
     */
    public double add(int i, double v) {
        checkFrozen();
        int idx = arrayIndex(i);
        final double old = data[idx];
        data[idx] = v + old;
        return old;
    }

    /**
     * Fill the vector with a value.
     *
     * @param v The value with which to fill the vector.
     */
    public void fill(double v) {
        checkFrozen();
        for (int i = offset; i < dataBound; i += stride) {
            data[i] = v;
        }
    }

    /**
     * Copy a vector into this vector.
     *
     * @param v The vector to copy in.
     */
    public void set(Vec v) {
        checkFrozen();
        Preconditions.checkArgument(v.size() == size(), "incompatible vector dimensions");
        if (stride == 1 && v.stride == 1) {
            System.arraycopy(v.data, v.offset, data, offset, size);
        } else {
            for (int i = offset, j = v.offset; i < dataBound;
                 i += stride, j += v.stride) {
                assert j < v.dataBound;
                data[i] = v.data[j];
            }
        }
    }

    /**
     * Copy an array into this vector.
     *
     * @param v The array to copy in.
     */
    public void set(double... v) {
        set(MutableVec.wrap(v));
    }

    /**
     * Add another vector to this vector.
     * @param v The other vector.
     * @throws IllegalArgumentException if {@code v} has a different dimension than this vector.
     */
    public void add(Vec v) {
        checkFrozen();
        Preconditions.checkArgument(v.size() == size(), "incompatible vector dimensions");
        for (int i = offset, j = v.offset; i < dataBound;
             i += stride, j += v.stride) {
            assert j < v.dataBound;
            data[i] += v.data[j];
        }
    }

    /**
     * Scale this vector.
     * @param s The scalar to multiply this vector by.
     */
    public void scale(double s) {
        checkFrozen();
        for (int i = offset; i < dataBound; i += stride) {
            data[i] *= s;
        }
    }

    @Override
    public MutableVec subVector(int offset, int size) {
        return subVector(offset, size, 1);
    }

    @Override
    public MutableVec subVector(int voff, int vsize, int vstride) {
        Preconditions.checkPositionIndex(voff, size, "offset");
        Preconditions.checkArgument(vstride >= 1, "stride is not positive");
        if (vsize > 0) {
            Preconditions.checkPositionIndex(voff + (vsize - 1) * vstride, size, "upper bound");
        }
        int noff = offset + voff * stride;
        int nstride = vstride * stride;
        return new MutableVec(data, noff, vsize, nstride, rootVector);
    }

    /**
     * Convert this vector to an immutable vector.  The storage is reused, if possible; this vector
     * cannot be modified once it is frozen.
     *
     * @return An immutable vector created from this vector.
     */
    public ImmutableVec freeze() {
        rootVector.frozen = true;
        return new ImmutableVec(data, offset, size, stride);
    }
}
