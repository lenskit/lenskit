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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.io.ObjectStream;

import java.util.List;

/**
 * Fluent interface for DAO queries.  This interface is immutable - new objects are always returned - so it is safe
 * to save partial queries and use them to build up more sophisticated queries.
 *
 * An implementation on top of the base DAO methods is used by {@link AbstractDataAccessObject}.  Other DAO
 * implementations can reimplement this interface in other ways, for example to generate SQL queries.
 *
 * @param <E> The entity type.
 */
public interface Query<E extends Entity> {
    /**
     * Add an attribute value condition to the query.
     * @param name The attribute name.
     * @param value The attribute value.
     * @param <T> The attribute type.
     * @return A query.
     */
    <T> Query<E> withAttribute(TypedName<T> name, T value);

    /**
     * Sort the query results by a field.
     * @param name The field name.
     * @return A query.
     */
    Query<E> orderBy(TypedName<? extends Comparable<?>> name);

    /**
     * Sort the query results by a field.
     * @param name The field name.
     * @param order The sort order.
     * @return A query.
     */
    Query<E> orderBy(TypedName<? extends Comparable<?>> name, SortOrder order);

    /**
     * View results as a different type.
     * @param type The entity view type.
     * @param <V> The entity view type.
     * @return A query.
     */
    <V extends Entity> Query<V> asType(Class<V> type);

    /**
     * Group the results of this query by an attribute.
     * @param name The attribute name to group by.
     * @return A grouped query.
     */
    GroupedQuery<E> groupBy(TypedName<Long> name);

    /**
     * Group the results of this query by an attribute.
     * @param name The attribute name to group by.
     * @return A grouped query.
     */
    GroupedQuery<E> groupBy(String name);

    /**
     * Stream the results of this query.
     * @return A stream of the results of this query.
     */
    ObjectStream<E> stream();

    /**
     * Get the results of this query as a list.
     * @return A list of the results of this query.
     */
    List<E> get();

    /**
     * Get the number of results the query would return.
     * @return The number of entities returned by the query.
     */
    int count();

    /**
     * Get the set of values from an attribute in the entities in this query.  Use this to do
     * things like get the set of items referenced in a user's ratings:
     *
     * ```
     * dao.query(Rating.class)
     *    .withAttribute(CommonAttributes.USER_ID, user)
     *    .valueSet(CommonAttributes.ITEM_ID);
     * ```
     *
     * @param attr The attribute name to select.
     * @return The set of values `attribute` takes on in the query.
     */
    LongSet valueSet(TypedName<Long> attr);
}
