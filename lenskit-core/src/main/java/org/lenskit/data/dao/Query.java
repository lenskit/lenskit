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
