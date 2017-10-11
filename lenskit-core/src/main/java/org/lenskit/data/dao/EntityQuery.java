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

import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import org.lenskit.data.entities.Attribute;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Representation of a query.
 * @param <E> The entity type.
 * @see #newBuilder(EntityType)
 */
public class EntityQuery<E extends Entity> implements Predicate<Entity> {
    private EntityType entityType;
    private List<Attribute<?>> filterFields;
    private List<SortKey> sortKeys;
    private Class<E> viewType;

    EntityQuery(EntityType et, List<Attribute<?>> filt, List<SortKey> sort,
                Class<E> view) {
        entityType = et;
        filterFields = filt;
        sortKeys = sort;
        viewType = view;
    }

    /**
     * Construct a new data query builder.
     * @param type The entity type
     * @return The query builder.
     */
    public static EntityQueryBuilder newBuilder(EntityType type) {
        return new EntityQueryBuilder(type);
    }

    /**
     * Get the entity type to return.
     * @return The type of entities to return.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Get the field filters. The results should only include entities matching *all* queries.
     * @return A map of attributes to objects to filter.
     */
    public List<Attribute<?>> getFilterFields() {
        return filterFields;
    }

    /**
     * Get the sort keys.
     * @return The list of keys to sort by.
     */
    public List<SortKey> getSortKeys() {
        return sortKeys;
    }

    /**
     * Get the sort order as an ordering.
     * @return An ordering representing the {@linkplain #getSortKeys() sort order} of this query.
     */
    public Ordering<Entity> getOrdering() {
        Ordering<Entity> ord = null;
        for (SortKey k: sortKeys) {
            if (ord == null) {
                ord = k.ordering();
            } else {
                ord = ord.compound(k.ordering());
            }
        }
        return ord;
    }

    /**
     * Get the type to view results as.
     * @return The type to view results as.
     */
    public Class<E> getViewType() {
        return viewType;
    }

    /**
     * Call {@link #matches(Entity)}.
     * @param entity The entity to match.
     * @return `true` if the entity matches the query
     */
    @Override
    public boolean apply(@Nullable Entity entity) {
        return entity != null && matches(entity);
    }

    /**
     * Determine whether this query matches the specified entity.
     * @param entity The entity to test.
     * @return The entity.
     */
    public boolean matches(Entity entity) {
        if (!entity.getType().equals(entityType)) {
            return false;
        }
        for (Attribute<?> attr: filterFields) {
            Object val = entity.maybeGet(attr.getTypedName());
            if (!attr.getValue().equals(val)) {
                return false;
            }
        }
        return true;
    }
}
