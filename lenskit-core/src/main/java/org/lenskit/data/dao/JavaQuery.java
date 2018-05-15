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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.io.ObjectStream;

import java.util.List;
import java.util.stream.Collectors;

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
        return stream().collect(Collectors.toList());
    }

    @Override
    public int count() {
        return (int) stream().count();
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
