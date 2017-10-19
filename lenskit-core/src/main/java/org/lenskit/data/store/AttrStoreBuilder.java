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
