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
package org.lenskit.data.dao;

import com.google.common.collect.ImmutableMap;
import org.lenskit.data.entities.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for entity collection DAOs.
 */
@NotThreadSafe
public class EntityCollectionDAOBuilder {
    private Map<EntityType, EntityCollectionBuilder> entitySets = new HashMap<>();
    // remember the last builder used as a fast path
    private EntityCollectionBuilder lastBuilder = null;
    private EntityType last = null;

    /**
     * Index entities by an attribute.
     * @param et The entity type.
     * @param attr The attribute to index.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder addIndex(EntityType et, TypedName<?> attr) {
        EntityCollectionBuilder builder = findBuilder(et);
        builder.addIndex(attr);
        return this;
    }

    /**
     * Add an entity to the DAO.
     * @param e The entity to add
     * @return The DAO builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntity(Entity e) {
        EntityType type = e.getType();
        EntityCollectionBuilder bld = findBuilder(type);
        bld.add(e);

        return this;
    }

    private EntityCollectionBuilder findBuilder(EntityType type) {
        if (type != last) {
            lastBuilder = entitySets.get(type);
            last = type;
            if (lastBuilder == null) {
                lastBuilder = new EntityCollectionBuilder(type);
                entitySets.put(type, lastBuilder);
            }
        }
        assert lastBuilder != null;
        return lastBuilder;
    }

    /**
     * Add multiple entities.
     * @param entities The entity list.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntities(Entity... entities) {
        return addEntities(Arrays.asList(entities));
    }

    /**
     * Add multiple entities.
     * @param entities The entity list.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntities(Iterable<Entity> entities) {
        for (Entity e: entities) {
            addEntity(e);
        }

        return this;
    }

    public EntityCollectionDAO build() {
        ImmutableMap.Builder<EntityType, EntityCollection> mb = ImmutableMap.builder();
        for (Map.Entry<EntityType, EntityCollectionBuilder> e: entitySets.entrySet()) {
            mb.put(e.getKey(), e.getValue().build());
        }

        return new EntityCollectionDAO(mb.build());
    }
}
