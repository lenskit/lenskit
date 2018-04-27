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
