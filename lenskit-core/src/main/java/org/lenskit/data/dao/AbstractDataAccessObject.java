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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import org.lenskit.data.entities.DefaultEntityType;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.GroupingObjectStream;
import org.lenskit.util.io.ObjectStream;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Helper class to make it easier to create DAOs.  This base class implements several of the DAO
 * convenience methods in terms of the master {@link #streamEntities(EntityQuery)} method, so that
 * DAO implementers only need to translate {@link EntityQuery} objects into their underlying query
 * framework (collection predicates, Jooq queries, HQL, or whatever).
 */
public abstract class AbstractDataAccessObject implements DataAccessObject {
    private final ConcurrentMap<Class<?>,EntityType> viewClassCache = new ConcurrentSkipListMap<>(Ordering.arbitrary());

    @Override
    public Query<Entity> query(EntityType type) {
        return new JavaQuery<>(this, type, Entity.class);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation uses an internal {@link Query} implementation to prepare queries for
     * {@link #streamEntities(EntityQuery)} and {@link #streamEntityGroups(EntityQuery, TypedName)}.
     */
    @Override
    public <V extends Entity> Query<V> query(Class<V> type) {
        EntityType etype = viewClassCache.get(type);
        if (etype == null) {
            DefaultEntityType det = type.getAnnotation(DefaultEntityType.class);
            if (det == null) {
                throw new IllegalArgumentException(type + " has no default entity type annotation");
            }
            etype = EntityType.forName(det.value());
            viewClassCache.put(type, etype);
        }
        return new JavaQuery<>(this, etype, type);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #streamEntities(EntityQuery)}
     */
    @Override
    public ObjectStream<Entity> streamEntities(EntityType type) {
        return streamEntities(EntityQuery.newBuilder(type).build());
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #streamEntities(EntityType)} and groups the result.
     */
    @Override
    public <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> query, TypedName<Long> grpCol) {
        EntityQueryBuilder qb = EntityQuery.newBuilder(query.getEntityType());
        qb.addFilterFields(query.getFilterFields());
        qb.addSortKey(grpCol);
        qb.addSortKeys(query.getSortKeys());
        ObjectStream<E> stream = streamEntities(qb.buildWithView(query.getViewType()));
        return new GroupStream<>(stream, grpCol);
    }

    private static class GroupStream<E extends Entity> extends GroupingObjectStream<IdBox<List<E>>, E> {
        private final TypedName<Long> attribute;
        private long id;
        private ImmutableList.Builder<E> builder;

        GroupStream(@WillCloseWhenClosed ObjectStream<E> base, TypedName<Long> attr) {
            super(base);
            attribute = attr;
        }

        @Override
        protected void clearGroup() {
            builder = null;
        }

        @Override
        protected boolean handleItem(@Nonnull E item) {
            if (builder == null) {
                id = item.getLong(attribute);
                builder = ImmutableList.builder();
            } else if (id != item.getLong(attribute)) {
                return false;
            }
            builder.add(item);
            return true;
        }

        @Nonnull
        @Override
        protected IdBox<List<E>> finishGroup() {
            IdBox<List<E>> box = IdBox.create(id, (List<E>) builder.build());
            builder = null;
            return box;
        }
    }
}
