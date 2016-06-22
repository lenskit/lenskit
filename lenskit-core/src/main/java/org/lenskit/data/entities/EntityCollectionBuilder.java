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
import com.google.common.collect.ImmutableMap;
import org.lenskit.data.dao.EntityIndex;
import org.lenskit.data.dao.EntityIndexBuilder;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for entity collections.
 */
public class EntityCollectionBuilder {
    private final EntityType type;
    private final KeyedObjectMapBuilder<Entity> store;
    private final Map<String,EntityIndexBuilder> indexBuilders;

    public EntityCollectionBuilder(EntityType type) {
        this.type = type;
        store = KeyedObjectMap.newBuilder(Entities.idKeyExtractor());
        indexBuilders = new HashMap<>();
    }

    /**
     * Add an index to an entity collection to speed up lookups.
     * @param attribute The attribute to index.
     * @param <T> The attribute type
     * @return The builder (for chaining).
     */
    public <T> EntityCollectionBuilder addIndex(TypedName<T> attribute) {
        if (indexBuilders.containsKey(attribute.getName())) {
            throw new IllegalStateException("attribute " + attribute.getName() + " already indexed");
        }
        EntityIndexBuilder ib = EntityIndexBuilder.create(attribute);
        indexBuilders.put(attribute.getName(), ib);

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
        for (EntityIndexBuilder ib: indexBuilders.values()) {
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
        ImmutableMap.Builder<String,EntityIndex> indexes = ImmutableMap.builder();
        for (Map.Entry<String,EntityIndexBuilder> e: indexBuilders.entrySet()) {
            indexes.put(e.getKey(), e.getValue().build());
        }
        return new EntityCollection(type, store.build(), indexes.build());
    }
}
