/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.*;

import java.io.Serializable;
import java.util.Collection;

/**
 * Immutable key index backed by a hash table.
 */
public final class FrozenHashKeyIndex implements KeyIndex, Serializable {
    private static final long serialVersionUID = 1L;

    private Long2IntMap indexMap;
    private LongArrayList keyList;

    /**
     * Construct a new key index.  It maps each long key to its position in the list.
     *
     * @param keys The list of keys to store.
     */
    FrozenHashKeyIndex(LongList keys) {
        keyList = new LongArrayList(keys);
        indexMap = new Long2IntOpenHashMap(keys.size());
        indexMap.defaultReturnValue(-1);
        LongListIterator iter = keys.listIterator();
        while (iter.hasNext()) {
            int idx = iter.nextIndex();
            long key = iter.next();
            int old = indexMap.put(key, idx);
            if (old >= 0) {
                throw new IllegalArgumentException("key " + key + " appears multiple times");
            }
        }
    }

    /**
     * Construct a new key index with pre-built storage.
     */
    FrozenHashKeyIndex(Long2IntMap indexes, LongList keys) {
        indexMap = new Long2IntOpenHashMap(indexes);
        keyList = new LongArrayList(keys);
    }

    /**
     * Construct a new key index.  It maps each long key to its position in the list.
     *
     * @param keys The list of keys to store.
     */
    public static FrozenHashKeyIndex create(LongList keys) {
        return new FrozenHashKeyIndex(keys);
    }

    @Override
    public int getIndex(long id) {
        int idx = tryGetIndex(id);
        if (idx < 0) {
            throw new IllegalArgumentException("key " + id + " not in index");
        } else {
            return idx;
        }
    }

    @Override
    public boolean containsKey(long id) {
        return tryGetIndex(id) >= 0;
    }

    @Override
    public long getKey(int idx) {
        return keyList.getLong(idx);
    }

    @Override
    public LongList getKeyList() {
        return LongLists.unmodifiable(keyList);
    }

    @Override
    public int tryGetIndex(long id) {
        return indexMap.get(id);
    }

    @Override
    public int size() {
        return keyList.size();
    }

    @Override
    public int getLowerBound() {
        return 0;
    }

    @Override
    public int getUpperBound() {
        return size();
    }
}
