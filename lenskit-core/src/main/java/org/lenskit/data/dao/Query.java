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

import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;

import java.util.List;

/**
 * Fluent interface for DAO queries.  This interface is immutable - new objects are always returned - so it is safe
 * to save partial queries and use them to build up more sophisticated queries.
 *
 * @param <E> The entity type.
 */
public class Query<E extends Entity> {
    private final DataAccessObject dao;
    private final EntityQueryBuilder builder;
    private final Class<E> viewClass;

    /**
     * Construct a new query.  This should only be used by DAO implementations.
     * @param type The entity type.
     * @param view The view type.
     */
    Query(DataAccessObject dao, EntityType type, Class<E> view) {
        this(dao, EntityQuery.newBuilder(type), view);
    }

    Query(DataAccessObject dao, EntityQueryBuilder eqb, Class<E> view) {
        this.dao = dao;
        builder = eqb;
        viewClass = view;
    }

    /**
     * Add an attribute value condition to the query.
     * @param name The attribute name.
     * @param value The attribute value.
     * @param <T> The attribute type.
     * @return A query.
     */
    public <T> Query<E> withAttribute(TypedName<T> name, T value) {
        return new Query<>(dao, builder.copy().addFilterField(name, value), viewClass);
    }

    /**
     * Sort the query results by a field.
     * @param name The field name.
     * @return A query.
     */
    public Query<E> orderBy(TypedName<? extends Comparable<?>> name) {
        return new Query<>(dao, builder.copy().addSortKey(name), viewClass);
    }

    /**
     * Sort the query results by a field.
     * @param name The field name.
     * @param order The sort order.
     * @return A query.
     */
    public Query<E> orderBy(TypedName<? extends Comparable<?>> name, SortOrder order) {
        return new Query<>(dao, builder.copy().addSortKey(name, order), viewClass);
    }

    /**
     * View results as a different type.
     * @param type The entity view type.
     * @param <V> The entity view type.
     * @return A query.
     */
    public <V extends Entity> Query<V> asType(Class<V> type) {
        return new Query<>(dao, builder, type);
    }

    /**
     * Stream the results of this query.
     * @return A stream of the results of this query.
     */
    public ObjectStream<E> stream() {
        return dao.streamEntities(builder.buildWithView(viewClass));
    }

    /**
     * Get the results of this query as a list.
     * @return A list of the results of this query.
     */
    public List<E> get() {
        ImmutableList.Builder<E> builder = ImmutableList.builder();
        try (ObjectStream<E> stream = stream()) {
            builder.addAll(stream);
        }
        return builder.build();
    }

    /**
     * Stream the results of this query as a list, grouped by an attribute.
     * @return A list of the results of this query.
     */
    public ObjectStream<IdBox<List<E>>> streamGrouped(TypedName<Long> attr) {
        return dao.streamEntityGroups(builder.buildWithView(viewClass), attr);
    }
}
