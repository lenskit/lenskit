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
package org.lenskit.data.entities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;

import java.util.*;

/**
 * Builder class for entity collections.
 */
public class EntityCollectionBuilder {
    private final EntityType type;
    private final KeyedObjectMapBuilder<Entity> store;
    private final Map<String,IndexBuilder> indexes;

    public EntityCollectionBuilder(EntityType type) {
        this.type = type;
        store = KeyedObjectMap.newBuilder(Entities.idKeyExtractor());
        indexes = new HashMap<>();
    }

    /**
     * Add an index to an entity collection to speed up lookups.
     * @param attribute The attribute to index.
     * @param <T> The attribute type
     * @return The builder (for chaining).
     */
    public <T> EntityCollectionBuilder addIndex(TypedName<T> attribute) {
        if (indexes.containsKey(attribute.getName())) {
            throw new IllegalStateException("attribute " + attribute.getName() + " already indexed");
        }
        IndexBuilder ib;
        if (attribute.getType().equals(LongIndexBuilder.class)) {
            ib = new LongIndexBuilder((TypedName<Long>) attribute);
        } else {
            ib = new GenericIndexBuilder(attribute);
        }
        indexes.put(attribute.getName(), ib);

        for (Entity e: store.build()) {
            ib.add(e);
        }

        return this;
    }

    /**
     * Add an index to an entity collection to speed up lookups.
     * @param attrName The name of the attribute to index.
     * @return The builder (for chaining).
     */
    public EntityCollectionBuilder addIndex(String attrName) {
        return addIndex(TypedName.create(attrName, Object.class));
    }

    /**
     * Add an entity to the collection.
     * @param e The entity to add.
     * @return The builder (for chaining).
     */
    public EntityCollectionBuilder add(Entity e) {
        Preconditions.checkArgument(e.getType().equals(type));
        store.add(e);
        for (IndexBuilder ib: indexes.values()) {
            ib.add(e);
        }
        return this;
    }

    /**
     * Add multiple entities to the collection.
     * @param entities The entity to add.
     * @return The builder (for chaining).
     */
    public EntityCollectionBuilder addAll(Entity... entities) {
        return addAll(Arrays.asList(entities));
    }

    /**
     * Add multiple entities to the collection.
     * @param entities The entity to add.
     * @return The builder (for chaining).
     */
    public EntityCollectionBuilder addAll(Iterable<Entity> entities) {
        for (Entity e: entities) {
            add(e);
        }
        return this;
    }

    /**
     * Build the entity collection.
     * @return The collection of entities.
     */
    public EntityCollection build() {
        return new EntityCollection(type, store.build());
    }

    static interface IndexBuilder {
        void add(Entity e);
    }

    static class GenericIndexBuilder implements IndexBuilder {
        TypedName<?> attribute;
        Map<Object,ImmutableList.Builder<Entity>> data;

        GenericIndexBuilder(TypedName<?> attr) {
            attribute = attr;
            data = new HashMap<>();
        }

        @Override
        public void add(Entity e) {
            Object value = e.get(attribute);
            ImmutableList.Builder<Entity> lb = data.get(value);
            if (lb == null) {
                lb = ImmutableList.builder();
                data.put(value, lb);
            }
            lb.add(e);
        }
    }

    static class LongIndexBuilder implements IndexBuilder {
        TypedName<Long> attribute;
        Long2ObjectMap<ImmutableList.Builder<Entity>> data;

        LongIndexBuilder(TypedName<Long> attr) {
            attribute = attr;
            data = new Long2ObjectOpenHashMap<>();
        }

        @Override
        public void add(Entity e) {
            long value = e.getLong(attribute);
            ImmutableList.Builder<Entity> lb = data.get(value);
            if (lb == null) {
                lb = ImmutableList.builder();
                data.put(value, lb);
            }
            lb.add(e);
        }
    }
}
