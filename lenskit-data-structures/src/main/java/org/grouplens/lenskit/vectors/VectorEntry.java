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

import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * An entry in a sparse vector. This represents the key-value pair at one entry
 * in a sparse vector, similar to {@link java.util.Map.Entry} does for maps.
 * <p>
 * The entry class does not support a public setValue method; to set the
 * value at an entry on a mutable sparse vector, use
 * {@link MutableSparseVector#set(VectorEntry, double)}.
 * This design allows the same VectorEntry to work for both Mutable
 * and Immutable vectors, since the Immutable vectors can safely
 * return the VectorEntry knowing it cannot change the (immutable)
 * values, while the set operation on Mutable vectors allows the
 * element to be changed.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @since 0.11
 */
public final class VectorEntry implements Cloneable {
    /**
     * The state of an entry in a sparse vector.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     * @since 0.11
     */
    public static enum State {
        /**
         * A set entry.
         */
        SET,
        /**
         * An unset entry.
         */
        UNSET,
        /**
         * Either entry state — used for requesting all entries.
         */
        EITHER
    }

    @Nullable
    private final SparseVector vector;
    private int index;
    private long key;
    private double value;
    private boolean isSet;

    /**
     * Construct a new vector entry for a particular vector.
     *
     * @param vec The vector this entry is from.
     * @param i   The index in the vector of this entry.
     * @param k   The entry's key.
     * @param val The entry's value.
     * @param set Whether the entry is set.
     */
    VectorEntry(@Nullable SparseVector vec, int i, long k, double val, boolean set) {
        vector = vec;
        index = i;
        key = k;
        value = val;
        isSet = set;
    }

    /**
     * Construct a new vector entry.
     *
     * @param k The entry's key.
     * @param v The entry's value.
     */
    public VectorEntry(long k, double v) {
        this(null, -1, k, v, true);
    }

    /**
     * Get the key at this entry.
     *
     * @return The key of this entry.
     */
    public long getKey() {
        return key;
    }

    /**
     * Get the value at this entry.
     *
     * @return The value of this entry.
     */
    public double getValue() {
        return value;
    }

    /**
     * Internal method to get associated index, if specified.
     *
     * @return The index into the vector of this entry.
     */
    int getIndex() {
        return index;
    }

    /**
     * Query whether this entry is set.
     *
     * @return {@code true} if the entry's key is in the key set; {@code false} if
     *         it is only in the key domain.
     */
    public boolean isSet() {
        return isSet;
    }

    /**
     * Update the entry (used for fast iteration).
     *
     * @param i The new index.
     * @param k The new key.
     * @param v the new value.
     * @param set Whether the entry is set.
     */
    void set(int i, long k, double v, boolean set) {
        index = i;
        key = k;
        value = v;
        isSet = set;
    }

    /**
     * Update the value. Used only to implement {@link MutableSparseVector#set(VectorEntry, double)}.
     *
     * @param v The new value
     */
    void setValue(double v) {
        value = v;
    }

    /**
     * Get the sparse vector associated with this entry.  An entry does not necessarily have a
     * reference to a vector.
     *
     * @return The associated vector, or {@code null} if no vector is linked.
     */
    @Nullable
    public SparseVector getVector() {
        return vector;
    }

    @Override
    public VectorEntry clone() {
        VectorEntry e;
        try {
            e = (VectorEntry) super.clone();
        } catch (CloneNotSupportedException exc) {
            throw new AssertionError(exc); // This cannot happen
        }
        return e;
    }
    
    @Override
    public String toString() {
        return "VectorEntry:"
                + " vector=" + vector
                + " index=" + index 
                + " key=" + key 
                + " value=" + value
                + " isSet=" + isSet;
    }

    /**
     * A function that copies (clones) vector entries.
     * @return A function that copies vector entries.
     */
    static Function<VectorEntry, VectorEntry> copyFunction() {
        return CopyFunction.INSTANCE;
    }
    private static enum CopyFunction implements Function<VectorEntry,VectorEntry> {
        INSTANCE;

        @Nullable
        @Override
        public VectorEntry apply(@Nullable VectorEntry input) {
            if (input == null) {
                return null;
            } else {
                return input.clone();
            }
        }

    }
}
