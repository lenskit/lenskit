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
package org.lenskit.data.dao;

import com.google.common.collect.Ordering;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.lenskit.data.entities.*;
import org.lenskit.data.store.EntityCollection;
import org.lenskit.util.IdBox;
import org.lenskit.util.describe.Describable;
import org.lenskit.util.describe.DescriptionWriter;
import org.lenskit.util.io.AbstractObjectStream;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        ObjectStream<E> stream = query.getViewType().equals(Entity.class)
                ? (ObjectStream<E>) baseStream
                : ObjectStreams.transform(baseStream, Entities.projection(query.getViewType()));
        List<SortKey> sort = query.getSortKeys();
        List<SortKey> dataKeys = data.getSortKeys();
        // already sorted if sort is a prefix of data keys
        boolean alreadyInOrder = sort.size() <= dataKeys.size();
        for (int i = 0; alreadyInOrder && i < sort.size(); i++) {
            if (!sort.get(i).equals(dataKeys.get(i))) {
                // oops, we want to sort by sth that isn't pre-sorted.
                alreadyInOrder = false;
            }
        }
        if (alreadyInOrder) {
            return stream;
        }

        // we must sort; need to make list ourselves since makeList lists are immutable
        Ordering<Entity> ord = query.getOrdering();
        assert ord != null;
        try {
            return ObjectStreams.wrap(ord.immutableSortedCopy(stream));
        } finally {
            stream.close();
        }
    }

    @Override
    public <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> query, TypedName<Long> grpCol) {
        EntityCollection data = storage.get(query.getEntityType());
        if (data == null) {
            return ObjectStreams.empty();
        }

        Map<Long, List<Entity>> groups = data.grouped(grpCol);
        return new AbstractObjectStream<IdBox<List<E>>>() {
            Iterator<Map.Entry<Long, List<Entity>>> iter = groups.entrySet().iterator();

            @Override
            public IdBox<List<E>> readObject() {
                while (iter.hasNext()) {
                    Map.Entry<Long, List<Entity>> entry = iter.next();
                    Stream<Entity> data = entry.getValue()
                                               .stream()
                                               .filter(query);
                    Ordering<Entity> ord = query.getOrdering();
                    if (ord != null) {
                        data = data.sorted(ord);
                    }
                    List<E> list = data.map(Entities.projection(query.getViewType()))
                                       .collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        return IdBox.create(entry.getKey(), list);
                    }
                }

                // we're done
                return null;
            }
        };
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
