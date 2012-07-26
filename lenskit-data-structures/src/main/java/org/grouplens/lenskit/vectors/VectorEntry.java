/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import javax.annotation.Nullable;

/**
 * An entry in a vector. This represents the key-value pair at one entry
 * in a vector, similar to {@link java.util.Map.Entry} does for maps.
 * <p>
 * The entry class does not support a public setValue method; to set the
 * value at an entry on a mutable sparse vector, use
 * {@link MutableSparseVector#set(VectorEntry, double)}.
 *
 * @author Michael Ekstrand
 */
public final class VectorEntry {
    @Nullable
    final SparseVector vector;
    private int index;
    private long key;
    private double value;

    /**
     * Construct a new vector entry for a particular vector.
     * @param vec The vector this entry is from.
     * @param i The index in the vector of this entry.
     * @param k The entry's key.
     * @param val The entry's value.
     */
    VectorEntry(@Nullable SparseVector vec, int i, long k, double val) {
        vector = vec;
        index = i;
        key = k;
        value = val;
    }

    /**
     * Construct a new vector entry.
     * @param k The entry's key.
     * @param v The entry's value.
     */
    public VectorEntry(long k, double v) {
        this(null, -1, k, v);
    }

    /**
     * Get the key at this entry.
     * @return The key of this entry.
     */
    public long getKey() {
        return key;
    }

    /**
     * Get the value at this entry.
     * @return The value of this entry.
     */
    public double getValue() {
        return value;
    }

    /**
     * Internal method to get associated index, if specified.
     * @return The index into the vector of this entry.
     */
    int getIndex() {
        return index;
    }

    /**
     * Update the entry (used for fast iteration).
     * @param i The new index.
     * @param k The new key.
     * @param v the new value.
     */
    void set(int i, long k, double v) {
        index = i;
        key = k;
        value = v;
    }

    /**
     * Update the entry (used for fast iteration).
     * @param k The new key.
     * @param v The new value.
     */
    void set(long k, double v) {
        key = k;
        value = v;
    }

    /**
     * Update the value. Used only to implement {@link MutableSparseVector#set(VectorEntry, double)}.
     * @param v The new value
     */
    void setValue(double v) {
        value = v;
    }
}
