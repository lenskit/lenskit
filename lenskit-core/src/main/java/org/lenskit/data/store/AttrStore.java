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

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.OptionalInt;

/**
 * An attribute store, storing a column of attributes.
 */
class AttrStore {
    private final ImmutableList<Shard> shards;
    private final int totalSize;

    AttrStore(List<Shard> shards, int total) {
        assert shards.stream()
                     .mapToInt(Shard::size)
                     .sum()
                == total;
        if (shards.size() > 1) {
            assert shards.subList(0, shards.size() - 1)
                         .stream()
                         .mapToInt(Shard::size)
                         .min()
                         .equals(OptionalInt.of(Shard.SHARD_SIZE));
            assert shards.subList(0, shards.size() - 1)
                         .stream()
                         .mapToInt(Shard::size)
                         .max()
                         .equals(OptionalInt.of(Shard.SHARD_SIZE));
        }

        this.shards = ImmutableList.copyOf(shards);
        this.totalSize = total;
    }

    /**
     * Get the number of attribute values stored.
     *
     * @return The number of values in this store.
     */
    int size() {
        return totalSize;
    }

    /**
     * Get the value at an index.
     *
     * @param idx The index.
     * @return The value at position `idx`, or `null` if there is no value.
     */
    Object get(int idx) {
        assert idx >= 0 && idx < totalSize;
        return shards.get(Shard.indexOfShard(idx))
                     .get(Shard.indexWithinShard(idx));
    }

    boolean isNull(int idx) {
        assert idx >= 0 && idx < totalSize;
        return shards.get(Shard.indexOfShard(idx))
                     .isNull(Shard.indexWithinShard(idx));
    }
}
