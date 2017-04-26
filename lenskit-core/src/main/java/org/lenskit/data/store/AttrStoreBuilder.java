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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Build an attribute store.
 */
class AttrStoreBuilder {
    private final Supplier<Shard> shardFactory;
    List<Shard> shards = new ArrayList<>();
    int size;

    AttrStoreBuilder() {
        this(ObjectShard::new);
    }

    AttrStoreBuilder(Supplier<Shard> sf) {
        shardFactory = sf;
    }

    /**
     * Get the number of attribute values stored so far.
     *
     * @return The number of values in this store.
     */
    int size() {
        return size;
    }

    /**
     * Skip the next attribute value (equivalent to adding `null`).
     */
    void skip() {
        int nexti = size;
        int si = Shard.indexOfShard(nexti);
        int vi = Shard.indexWithinShard(nexti);
        if (si >= shards.size()) {
            shards.add(shardFactory.get());
        }
        shards.get(si).put(vi, null);
        size += 1;
    }

    /**
     * Add a value to this store builder.
     *
     * @param val The value to add.
     */
    void add(Object val) {
        int nexti = size;
        int si = Shard.indexOfShard(nexti);
        int vi = Shard.indexWithinShard(nexti);
        Shard shard;
        if (si < shards.size()) {
            shard = shards.get(si);
        } else {
            shard = shardFactory.get();
            shards.add(shard);
        }
        Shard s2 = shard.adapt(val);
        if (s2 != shard) {
            shards.set(si, s2);
        }
        s2.put(vi, val);
        size += 1;
    }

    /**
     * Get the value at an index.
     *
     * @param idx The index.
     * @return The value at position `idx`, or `null` if there is no value.
     */
    Object get(int idx) {
        assert idx >= 0 && idx < size;
        int si = Shard.indexOfShard(idx);
        int vi = Shard.indexWithinShard(idx);
        return shards.get(si).get(vi);
    }

    /**
     * Swap the values at two indexes.  This is used for sorting.
     *
     * @param i The first index.
     * @param j The second index.
     */
    void swap(int i, int j) {
        int si = Shard.indexOfShard(i);
        int vi = Shard.indexWithinShard(i);
        int sj = Shard.indexOfShard(j);
        int vj = Shard.indexWithinShard(j);

        Shard shi = shards.get(si);
        Shard shj = shards.get(sj);

        Object valI = shi.get(vi);
        Object valJ = shj.get(vj);

        Shard shi2 = shi.adapt(valJ);
        if (shi2 != shi) {
            shards.set(si, shi2);
        }
        Shard shj2 = shj.adapt(valI);
        if (shj2 != shj) {
            shards.set(sj, shj2);
        }

        shi2.put(vi, valJ);
        shj2.put(vj, valI);
    }

    /**
     * Build the attribute store.
     *
     * @return The attribute store.
     */
    AttrStore build() {
        if (!shards.isEmpty()) {
            shards.get(shards.size() - 1).compact();
        }
        return new AttrStore(shards, size);
    }

    /**
     * Build a temp version of the attribute store.  It does not perform compacting cleanups.
     *
     * @return The attribute store.
     */
    AttrStore tempBuild() {
        return new AttrStore(shards, size);
    }
}
