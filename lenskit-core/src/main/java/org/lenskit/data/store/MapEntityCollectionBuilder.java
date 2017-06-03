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
