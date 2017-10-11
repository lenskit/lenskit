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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;

/**
 * A generic entity index builder.
 */
class LongEntityIndexBuilder extends EntityIndexBuilder {
    private final TypedName<Long> attributeName;
    private Long2ObjectMap<ImmutableList.Builder<Entity>> entityLists;

    /**
     * Construct a new index builder.
     * @param name The attribute name to index.
     */
    LongEntityIndexBuilder(String name) {
        this(TypedName.create(name, Long.class));
    }

    /**
     * Construct a new index builder.
     * @param name The attribute name to index.
     */
    LongEntityIndexBuilder(TypedName<Long> name) {
        attributeName = name;
        entityLists = new Long2ObjectOpenHashMap<>();
    }

    @Override
    public void add(Entity e) {
        Preconditions.checkState(entityLists != null, "build() already called");
        if (e.hasAttribute(attributeName)) {
            long value = e.getLong(attributeName);
            ImmutableList.Builder<Entity> lb = entityLists.get(value);
            if (lb == null) {
                lb = ImmutableList.builder();
                entityLists.put(value, lb);
            }
            lb.add(e);
        }
    }

    @Override
    public EntityIndex build() {
        Preconditions.checkState(entityLists != null, "build() already called");
        // arrange compact storage of the index
        KeyedObjectMapBuilder<IdBox<ImmutableList<Entity>>> bld = KeyedObjectMap.newBuilder();
        for (Long2ObjectMap.Entry<ImmutableList.Builder<Entity>> entry: entityLists.long2ObjectEntrySet()) {
            long value = entry.getLongKey();
            bld.add(IdBox.create(value, entry.getValue().build()));
            entry.setValue(null);
        }
        entityLists = null;
        return new LongEntityIndex(bld.build());
    }
}
