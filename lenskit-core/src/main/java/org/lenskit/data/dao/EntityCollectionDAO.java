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
package org.lenskit.data.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;
import org.lenskit.data.entities.*;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A DAO backed by one or more collections of entities.
 */
public class EntityCollectionDAO extends AbstractDataAccessObject implements Describable {
    private final Map<EntityType, EntityCollection> storage;

    EntityCollectionDAO(Map<EntityType, EntityCollection> data) {
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
    public static EntityCollectionDAO create(Collection<? extends Entity> data) {
        return newBuilder().addEntities(data).build();
    }


    @Override
    public Set<EntityType> getEntityTypes() {
        return storage.keySet();
    }

    @Override
    public LongSet getEntityIds(EntityType type) {
        EntityCollection entities = storage.get(type);
        if (entities != null) {
            return entities.idSet();
        } else {
            return LongSets.EMPTY_SET;
        }
    }

    @Nullable
    @Override
    public Entity lookupEntity(EntityType type, long id) {
        EntityCollection entities = storage.get(type);
        if (entities != null) {
            return entities.lookup(id);
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
        EntityCollection data = storage.get(query.getEntityType());
        if (data == null) {
            return ObjectStreams.empty();
        }

        ObjectStream<Entity> baseStream;
        List<Attribute<?>> filters = query.getFilterFields();
        if (filters.isEmpty()) {
            baseStream = ObjectStreams.wrap(data);
        } else {
            // optimize by trying to look up the first condition
            Attribute<?> f1 = filters.get(0);
            baseStream = ObjectStreams.wrap(data.find(f1));
            if (filters.size() > 1) {
                baseStream = ObjectStreams.filter(baseStream, query);
            }
        }

        ObjectStream<E> stream =
                ObjectStreams.transform(baseStream, Entities.projection(query.getViewType()));
        List<SortKey> sort = query.getSortKeys();
        if (sort.isEmpty()) {
            return stream;
        }

        // we must sort; need to make list ourselves since makeList lists are immutable
        ArrayList<E> list;
        try {
            list = Lists.newArrayList(stream);
        } finally {
            stream.close();
        }
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

    @Override
    public void describeTo(DescriptionWriter writer) {
        for (EntityType etype: Ordering.natural()
                                       .onResultOf(Entities.entityTypeNameFunction())
                                       .sortedCopy(storage.keySet())) {
            writer.putField(etype.getName(), storage.get(etype));
        }
    }
}
