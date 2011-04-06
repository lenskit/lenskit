/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.data.vector;

import java.util.Arrays;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import javax.annotation.concurrent.Immutable;

/**
 * Immutable sparse vectors.  These vectors cannot be changed, even by other code,
 * and are therefore safe to store and are thread-safe.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
public class ImmutableSparseVector extends SparseVector {
    private static final long serialVersionUID = -4740588973577998934L;

    /**
     * @param ratings
     */
    public ImmutableSparseVector(Long2DoubleMap ratings) {
        super(ratings);
    }

    /**
     * @param keys
     * @param values
     * @see SparseVector#SparseVector(long[], double[])
     */
    protected ImmutableSparseVector(long[] keys, double[] values) {
        super(keys, values);
    }
    
    @Override
    public ImmutableSparseVector clone() {
        return (ImmutableSparseVector) super.clone();
    }
    
    /**
     * Make an immutable version of a sparse vector.  If the source vector is
     * already immutable, it is returned.
     * @param v The vector to mirror immutably.
     * @return An immutable vector with the same contents as <var>v</var>.
     */
    public static ImmutableSparseVector make(SparseVector v) {
        if (v instanceof ImmutableSparseVector)
            return (ImmutableSparseVector) v;
        else
            return new ImmutableSparseVector(v.keys, Arrays.copyOf(v.values, v.size()));
    }

}
