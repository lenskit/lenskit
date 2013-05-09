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
import it.unimi.dsi.fastutil.doubles.DoubleArrays;

/**
 * Mutable {@link Vec}.  This vector can be modified and is not
 * thread-safe.
 *
 * @compat Experimental — this interface may change in future versions of LensKit.
 */
public class MutableVec extends Vec {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new mutable vector.  The vector is initialized to all 0's.
     *
     * @param dim The dimension of the vector.
     */
    public MutableVec(int dim) {
        this(new double[dim]);
    }

    /**
     * Construct a new vector. The array is <b>not</b> copied.
     * @param v The backing array.
     */
    private MutableVec(double[] v) {
        super(v);
    }

    /**
     * Create a new vector wrapping an existing array.
     *
     * @param data The data array.
     * @return A vector backed by {@code data}.
     */
    public static MutableVec wrap(double[] data) {
        return new MutableVec(data);
    }

    /**
     * Set a value in this vector.
     * @param i The index.
     * @param v The value to set.
     * @return The old value at {@code i}.
     * @throws IllegalArgumentException if {@code i} is not a valid index in this vector.
     */
    public double set(int i, double v) {
        Preconditions.checkElementIndex(i, size());
        final double old = data[i];
        data[i] = v;
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
        Preconditions.checkElementIndex(i, size());
        final double old = data[i];
        data[i] = v + old;
        return old;
    }

    /**
     * Fill the vector with a value.
     *
     * @param v The value with which to fill the vector.
     */
    public void fill(double v) {
        DoubleArrays.fill(data, v);
    }

    /**
     * Add another vector to this vector.
     * @param v The other vector.
     * @throws IllegalArgumentException if {@code v} has a different dimension than this vector.
     */
    public void add(Vec v) {
        Preconditions.checkArgument(v.size() == size(), "incompatible vector dimensions");
        final int sz = size();
        for (int i = 0; i < sz; i++) {
            data[i] += v.data[i];
        }
    }

    /**
     * Scale this vector.
     * @param s The scalar to multiply this vector by.
     */
    public void scale(double s) {
        final int sz = size();
        for (int i = 0; i < sz; i++) {
            data[i] *= s;
        }
    }
}
