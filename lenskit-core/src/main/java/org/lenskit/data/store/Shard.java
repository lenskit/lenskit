/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.store;

/**
 * A chunk of data for an attribute store.
 */
abstract class Shard {
    /* We want shard sizes to be a power of 2. Then we can strength-reduce the transformations.
     * We must manually strength-reduce because HotSpot doesn't do it.
     * This results in a small but measurable performance boost.
     */
    static final int SHARD_SIZE_POWER = 12;
    static final int SHARD_SIZE = 1 << SHARD_SIZE_POWER;
    static final int SHARD_MASK = SHARD_SIZE - 1;

    static int indexOfShard(int idx) {
        return idx >>> SHARD_SIZE_POWER;
    }

    static int indexWithinShard(int idx) {
        return idx & SHARD_MASK;
    }

    /**
     * Get the value at an index in the shard.
     * @param idx The index.
     * @return The value, or `null`.
     * @throws IndexOutOfBoundsException if `idx` is not a valid index.
     */
    abstract Object get(int idx);

    /**
     * Put a new value into the shard.
     * @param idx The index.
     * @param value The value (`null` to unset).
     */
    abstract void put(int idx, Object value);

    /**
     * Query whether a specified value is null.
     * @param idx The index to query.
     * @return `true` if the value at `idx` is `null` (or unset).
     */
    abstract boolean isNull(int idx);

    /**
     * Adapt this shard to be able to hold an object.
     * @param obj The object to store.
     * @return This shard, if it can hold the object, or a new shard that can.
     */
    abstract Shard adapt(Object obj);

    /**
     * Get the size (number of possibly-used values) in this shard.
     * @return The shard size.
     */
    abstract int size();

    /**
     * Compact this shard's storage to only the last used value.
     */
    abstract void compact();
}
