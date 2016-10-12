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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import java.util.List;

/**
 * Fluent interface for DAO queries.  This interface is immutable - new objects are always returned - so it is safe
 * to save partial queries and use them to build up more sophisticated queries.
 *
 * @param <E> The entity type.
 */
class JavaQuery<E extends Entity> implements Query<E> {
    private final DataAccessObject dao;
    private final EntityQueryBuilder builder;
    private final Class<E> viewClass;

    /**
     * Construct a new query.  This should only be used by DAO implementations.
     * @param type The entity type.
     * @param view The view type.
     */
    JavaQuery(DataAccessObject dao, EntityType type, Class<E> view) {
        this(dao, EntityQuery.newBuilder(type), view);
    }

    JavaQuery(DataAccessObject dao, EntityQueryBuilder eqb, Class<E> view) {
        this.dao = dao;
        builder = eqb;
        viewClass = view;
    }

    @Override
    public <T> Query<E> withAttribute(TypedName<T> name, T value) {
        return new JavaQuery<>(dao, builder.copy().addFilterField(name, value), viewClass);
    }

    @Override
    public Query<E> orderBy(TypedName<? extends Comparable<?>> name) {
        return new JavaQuery<>(dao, builder.copy().addSortKey(name), viewClass);
    }

    @Override
    public Query<E> orderBy(TypedName<? extends Comparable<?>> name, SortOrder order) {
        return new JavaQuery<>(dao, builder.copy().addSortKey(name, order), viewClass);
    }

    @Override
    public <V extends Entity> Query<V> asType(Class<V> type) {
        return new JavaQuery<>(dao, builder, type);
    }

    @Override
    public GroupedQuery<E> groupBy(TypedName<Long> name) {
        return new GroupedQuery<>(dao, builder.buildWithView(viewClass), name);
    }

    @Override
    public GroupedQuery<E> groupBy(String name) {
        return groupBy(TypedName.create(name, Long.class));
    }

    @Override
    public ObjectStream<E> stream() {
        return dao.streamEntities(builder.buildWithView(viewClass));
    }

    @Override
    public List<E> get() {
        return ObjectStreams.makeList(stream());
    }

    @Override
    public int count() {
        return ObjectStreams.count(stream());
    }

    @Override
    public LongSet valueSet(TypedName<Long> attr) {
        LongSet values = new LongOpenHashSet();
        try (ObjectStream<E> stream = stream()) {
            for (E entity: stream) {
                if (entity.hasAttribute(attr)) {
                    values.add(entity.getLong(attr));
                }
            }
        }
        return values;
    }
}
