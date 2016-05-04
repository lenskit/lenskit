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
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for entity collection DAOs.
 */
@NotThreadSafe
public class EntityCollectionDAOBuilder {
    private Map<EntityType, KeyedObjectMapBuilder<Entity>> entitySets = new HashMap<>();
    // remember the last builder used as a fast path
    private KeyedObjectMapBuilder<Entity> bld = null;
    private EntityType last = null;

    /**
     * Add an entity to the DAO.
     * @param e The entity to add
     * @return The DAO builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntity(Entity e) {
        EntityType type = e.getType();
        if (type != last) {
            bld = entitySets.get(type);
            last = type;
            if (bld == null) {
                bld = KeyedObjectMap.newBuilder(Entities.idKeyExtractor());
                entitySets.put(type, bld);
            }
        }
        assert bld != null;
        bld.add(e);

        return this;
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
        ImmutableMap.Builder<EntityType, KeyedObjectMap<Entity>> mb = ImmutableMap.builder();
        for (Map.Entry<EntityType, KeyedObjectMapBuilder<Entity>> e: entitySets.entrySet()) {
            mb.put(e.getKey(), e.getValue().build());
        }

        return new EntityCollectionDAO(mb.build());
    }
}
