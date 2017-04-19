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
        int si = idx / Shard.SHARD_SIZE;
        int vi = idx % Shard.SHARD_SIZE;
        return shards.get(si).get(vi);
    }

    boolean isNull(int idx) {
        int si = idx / Shard.SHARD_SIZE;
        int vi = idx % Shard.SHARD_SIZE;
        return shards.get(si).isNull(vi);
    }
}
