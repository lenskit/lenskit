/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
