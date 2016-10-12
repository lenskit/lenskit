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
package org.lenskit.data.entities;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.grouplens.lenskit.util.io.DescriptionWriter;
import org.grouplens.lenskit.util.io.Descriptions;
import org.grouplens.lenskit.util.io.HashDescriptionWriter;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder class for entity collections.  These builders are *destructive*: their {@link #build()} methods destroy the
 * internal storage, so the builder is single-use.
 */
public class EntityCollectionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EntityCollectionBuilder.class);
    private final EntityType type;
    private KeyedObjectMapBuilder<Entity> store;
    private Map<String,EntityIndexBuilder> indexBuilders;
    private Hasher hasher = Hashing.md5().newHasher();

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
        return add(e, true);
    }

    /**
     * Add an entity to the collection.
     * @param e The entity to add.
     * @param replace Whether to replace. If `false`, and an entity with the same ID as `e` has already been added,
     *                this entity is **silently** ignored.
     * @return The builder (for chaining).
     */
    public EntityCollectionBuilder add(Entity e, boolean replace) {
        Preconditions.checkState(store != null, "build() already called");
        Preconditions.checkArgument(e.getType().equals(type));
        if (!replace && store.containsKey(e.getId())) {
            return this;
        }

        store.add(e);
        hasher.putString(e.toString(), Charsets.UTF_8);
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
     * Get a view of the entities added, for iteration and re-processing.
     * @return The view of entities added.
     */
    public Collection<Entity> entities() {
        return store.objects();
    }

    /**
     * Build the entity collection.
     * @return The collection of entities.
     */
    public EntityCollection build() {
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
        return new EntityCollection(type, map, idxMap, hasher.hash());
    }
}
