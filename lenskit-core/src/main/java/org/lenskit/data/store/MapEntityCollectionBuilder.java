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
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for entity collections.  These builders are *destructive*: their {@link #build()} methods destroy the
 * internal storage, so the builder is single-use.
 */
class MapEntityCollectionBuilder extends EntityCollectionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MapEntityCollectionBuilder.class);
    private final EntityType type;
    private KeyedObjectMapBuilder<Entity> store;
    private Map<String,EntityIndexBuilder> indexBuilders;
    private Hasher hasher = Hashing.md5().newHasher();

    public MapEntityCollectionBuilder(EntityType type) {
        this.type = type;
        store = KeyedObjectMap.newBuilder(Entities.idKeyExtractor());
        indexBuilders = new HashMap<>();
    }

    @Override
    public <T> EntityCollectionBuilder addIndex(TypedName<T> attribute) {
        Preconditions.checkState(indexBuilders != null, "build() already called");
        if (indexBuilders.containsKey(attribute.getName())) {
            // already have that index
            return this;
        }
        EntityIndexBuilder ib = EntityIndexBuilder.create(attribute);
        indexBuilders.put(attribute.getName(), ib);

        for (Entity e: store.build()) {
            ib.add(e);
        }

        return this;
    }

    @Override
    public EntityCollectionBuilder addIndex(String attrName) {
        return addIndex(TypedName.create(attrName, Object.class));
    }

    @Override
    public EntityCollectionBuilder add(Entity e, boolean replace) {
        Preconditions.checkState(store != null, "build() already called");
        Preconditions.checkArgument(e.getType().equals(type));
        if (!replace && store.containsKey(e.getId())) {
            return this;
        }

        store.add(e);
        hasher.putInt(e.hashCode());
        for (EntityIndexBuilder ib: indexBuilders.values()) {
            ib.add(e);
        }
        return this;
    }

    @Override
    public Collection<Entity> entities() {
        return store.objects();
    }

    @Override
    public MapEntityCollection build() {
        Preconditions.checkState(store != null, "build() already called");
        ImmutableMap.Builder<String,EntityIndex> indexes = ImmutableMap.builder();
        for (Map.Entry<String,EntityIndexBuilder> e: indexBuilders.entrySet()) {
            indexes.put(e.getKey(), e.getValue().build());
        }
        KeyedObjectMap<Entity> map = store.build();
        ImmutableMap<String, EntityIndex> idxMap = indexes.build();
        logger.debug("built collection of {} entities with type {} and {} indexes",
                     map.size(), type, idxMap.size());
        store = null;
        indexBuilders = null;
        return new MapEntityCollection(type, map, idxMap, hasher.hash());
    }
}
