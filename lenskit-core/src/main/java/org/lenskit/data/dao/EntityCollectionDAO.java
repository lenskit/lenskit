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

import com.google.common.collect.Ordering;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A DAO backed by one or more collections of entities.
 */
public class EntityCollectionDAO extends AbstractDataAccessObject {
    private final Map<EntityType, KeyedObjectMap<Entity>> storage;

    EntityCollectionDAO(Map<EntityType, KeyedObjectMap<Entity>> data) {
        storage = data;
    }

    /**
     * Create a new DAO builder.
     * @return The entity collection DAO builder.
     */
    public static EntityCollectionDAOBuilder newBuilder() {
        return new EntityCollectionDAOBuilder();
    }

    /**
     * Create a new event collection DAO.
     * @param data The data to store in the DAO.
     * @return The DAO.
     */
    public static EntityCollectionDAO create(Entity... data) {
        return newBuilder().addEntities(data).build();
    }

    /**
     * Create a new event collection DAO.
     * @param data The data to store in the DAO.
     * @return The DAO.
     */
    public static EntityCollectionDAO create(Collection<Entity> data) {
        return newBuilder().addEntities(data).build();
    }


    @Override
    public Set<EntityType> getEntityTypes() {
        return storage.keySet();
    }

    @Override
    public LongSet getEntityIds(EntityType type) {
        KeyedObjectMap<Entity> entities = storage.get(type);
        if (entities != null) {
            return entities.keySet();
        } else {
            return LongSets.EMPTY_SET;
        }
    }

    @Nullable
    @Override
    public Entity lookupEntity(EntityType type, long id) {
        KeyedObjectMap<Entity> entities = storage.get(type);
        if (entities != null) {
            return entities.get(id);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public <E extends Entity> E lookupEntity(EntityType type, long id, Class<E> view) {
        Entity entity = lookupEntity(type, id);
        if (entity == null) {
            return null;
        } else {
            return Entities.project(entity, view);
        }
    }

    @Override
    public ObjectStream<Entity> streamEntities(EntityType type) {
        Iterable<Entity> data = storage.get(type);
        if (data != null) {
            return ObjectStreams.wrap(data.iterator());
        } else {
            return ObjectStreams.empty();
        }
    }

    @Override
    public <E extends Entity> ObjectStream<E> streamEntities(EntityQuery<E> query) {
        Iterable<Entity> data = storage.get(query.getEntityType());
        if (data == null) {
            return ObjectStreams.empty();
        }

        ObjectStream<E> stream =
                ObjectStreams.transform(ObjectStreams.filter(ObjectStreams.wrap(data.iterator()), query),
                                        Entities.projection(query.getViewType()));
        List<SortKey> sort = query.getSortKeys();
        if (sort.isEmpty()) {
            return stream;
        }

        // we must sort
        List<E> list = ObjectStreams.makeList(stream);
        Ordering<Entity> ord = null;
        for (SortKey k: sort) {
            if (ord == null) {
                ord = k.ordering();
            } else {
                ord = ord.compound(k.ordering());
            }
        }
        Collections.sort(list, ord);
        return ObjectStreams.wrap(list);
    }

}
