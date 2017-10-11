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
        indexMap.defaultReturnValue(-1);
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

    /**
     * Create a new key index.
     *
     * @param keys The keys.
     * @return A key index containing the elements of {@code keys}.
     */
    public static FrozenHashKeyIndex create(LongCollection keys) {
        if (keys instanceof  LongList) {
            return create((LongList) keys);
        } else {
            HashKeyIndex index = new HashKeyIndex();
            LongIterator iter = keys.iterator();
            while (iter.hasNext()) {
                index.internId(iter.nextLong());
            }
            return index.frozenCopy();
        }
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

    @Override
    public FrozenHashKeyIndex frozenCopy() {
        return this;
    }
}
