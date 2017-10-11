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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Builder for keyed object maps.
 * @param <T> The type of item.
 */
public class KeyedObjectMapBuilder<T> {
    private final KeyExtractor<? super T> extractor;
    private final List<T> builder;
    private final Long2IntMap posMap;

    public KeyedObjectMapBuilder(KeyExtractor<? super T> ex) {
        extractor = ex;
        builder = new ArrayList<>();
        posMap = new Long2IntOpenHashMap();
        posMap.defaultReturnValue(-1);
    }

    public KeyedObjectMapBuilder<T> add(T item) {
        long key = extractor.getKey(item);
        int pos = posMap.get(key);
        if (pos < 0) {
            pos = builder.size();
            posMap.put(key, pos);
            builder.add(item);
        } else {
            builder.set(pos, item);
        }
        return this;
    }

    public KeyedObjectMapBuilder<T> addAll(Iterable<? extends T> items) {
        for (T item: items) {
            add(item);
        }
        return this;
    }

    public KeyedObjectMapBuilder<T> add(T... items) {
        for (T item: items) {
            add(item);
        }
        return this;
    }

    /**
     * Get the objects that have been added so far.  Useful for re-processing them before finalizing the builder.
     * @return The objects added so far.
     */
    public Collection<T> objects() {
        return Collections.unmodifiableList(builder);
    }

    /**
     * Query whether an object with the specified key has been added.
     * @param key The key to query.
     * @return `true` if an object with key `key` has been added.
     */
    public boolean containsKey(long key) {
        return posMap.containsKey(key);
    }

    public KeyedObjectMap<T> build() {
        return new KeyedObjectMap<>(builder, extractor);
    }
}
