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
