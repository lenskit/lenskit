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
