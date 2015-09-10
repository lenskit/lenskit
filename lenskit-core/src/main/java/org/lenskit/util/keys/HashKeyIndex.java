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

/**
 * Mutable index mapping backed by a hash table.  This mapping allows indexes to be obtained before all keys have been
 * seen, making it very useful for incremental processing.
 */
public final class HashKeyIndex implements KeyIndex, Serializable {
    private static final long serialVersionUID = 1L;

    private Long2IntMap indexMap;
    private LongArrayList keyList;

    /**
     * Construct a new empty indexer.  The first interned ID will have index 0.
     */
    public HashKeyIndex() {
        indexMap = new Long2IntOpenHashMap();
        indexMap.defaultReturnValue(-1);
        keyList = new LongArrayList();
    }

    public static HashKeyIndex create() {
        return new HashKeyIndex();
    }

    /**
     * Construct a new indexer.  It maps each long key to its position in the list, and then be ready for more keys.
     *
     * @param keys The list of keys to store.
     */
    public static HashKeyIndex create(LongList keys) {
        HashKeyIndex idx = create();
        LongIterator iter = keys.iterator();
        while (iter.hasNext()) {
            idx.internId(iter.nextLong());
        }
        return idx;
    }

    /**
     * Convenience method to create a frozen key index.
     * @param keys The keys.
     * @return A frozen index.
     * @see FrozenHashKeyIndex#create(LongList)
     */
    public static FrozenHashKeyIndex createFrozen(LongList keys) {
        return FrozenHashKeyIndex.create(keys);
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

    /**
     * Get an index for a key, generating a new one if necessary.
     *
     * @param key The key.
     * @return The index for <var>key</var>. If the key has already been added to the index,
     *         the old index is returned; otherwise, a new index is generated and returned.
     */
    public int internId(long key) {
        int idx = tryGetIndex(key);
        if (idx < 0) {
            idx = keyList.size();
            keyList.add(key);
            indexMap.put(key, idx);
        }
        return idx;
    }

    /**
     * Make an immutable copy of this index mapping.
     *
     * @return An immutable copy of the index mapping.
     */
    public FrozenHashKeyIndex frozenCopy() {
        return new FrozenHashKeyIndex(indexMap, keyList);
    }
}
