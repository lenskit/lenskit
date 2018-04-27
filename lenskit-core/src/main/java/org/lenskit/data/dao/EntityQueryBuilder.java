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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.Attribute;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Data query builder exposing a fluent interface.
 */
public class EntityQueryBuilder {
    private EntityType entityType;
    private final ImmutableList.Builder<Attribute<?>> filter;
    private final ImmutableList.Builder<SortKey> sortKey;

    /**
     * Construct a new data query builder.
     * @param type The entity type.
     */
    public EntityQueryBuilder(EntityType type) {
        entityType = type;
        filter = ImmutableList.builder();
        sortKey = ImmutableList.builder();
    }

    /**
     * Set the entity type.
     * @param type The entity type.
     * @return The query builder (for chaining).
     * @deprecated Just provide a type at construction time.
     */
    @Deprecated
    public EntityQueryBuilder setEntityType(EntityType type) {
        entityType = type;
        return this;
    }

    /**
     * Add a filter condition to the builder.
     * @param name The attribute to filter.
     * @param obj The object to test for equality.
     * @param <A> The attribute type.
     * @return The query builder (for chaining).
     */
    public <A> EntityQueryBuilder addFilterField(TypedName<A> name, @Nonnull A obj) {
        return this.addFilterField(Attribute.create(name, obj));
    }

    /**
     * Add a filter condition to the builder.
     * @param condition The condition to add.
     * @param <A> The attribute type.
     * @return The query builder (for chaining).
     */
    public <A> EntityQueryBuilder addFilterField(Attribute<?> condition) {
        filter.add(condition);
        return this;
    }

    /**
     * Add a filter condition to the builder.
     * @param conditions The conditions to add.
     * @return The query builder (for chaining).
     */
    public EntityQueryBuilder addFilterFields(List<Attribute<?>> conditions) {
        for (Attribute<?> c: conditions) {
            addFilterField(c);
        }
        return this;
    }

    /**
     * Add an ascending sort key.
     * @param attr The attribute to sort by.
     * @return The query builder (for chaining).
     */
    public EntityQueryBuilder addSortKey(TypedName<? extends Comparable> attr) {
        return addSortKey(attr, SortOrder.ASCENDING);
    }

    /**
     * Add a sort key.
     * @param name The attribute to sort by.
     * @param order The sort order.
     * @return The query builder (for chaining).
     */
    public EntityQueryBuilder addSortKey(TypedName<? extends Comparable> name, SortOrder order) {
        return addSortKey(new SortKey(name, order));
    }

    /**
     * Add a sort key.
     * @param key The sort key to add.
     * @return The query builder (for chaining).
     */
    public EntityQueryBuilder addSortKey(SortKey key) {
        sortKey.add(key);
        return this;
    }

    /**
     * Add multiple sort keys.
     * @param keys The sort key to add.
     * @return The query builder (for chaining).
     */
    public EntityQueryBuilder addSortKeys(Iterable<SortKey> keys) {
        for (SortKey k: keys) {
            addSortKey(k);
        }
        return this;
    }

    /**
     * Create a new entity query builder that is a copy of this one. Future additions to either will not affect the
     * other.
     * @return A new query builder.
     */
    public EntityQueryBuilder copy() {
        EntityQueryBuilder eqb = new EntityQueryBuilder(entityType);
        eqb.addFilterFields(filter.build());
        eqb.addSortKeys(sortKey.build());
        return eqb;
    }

    /**
     * Build the data query.
     * @return The data query.
     */
    public EntityQuery<Entity> build() {
        return buildWithView(Entity.class);
    }

    /**
     * Build the data query with a view type.
     * @return The data query.
     */
    public <E extends Entity> EntityQuery<E> buildWithView(Class<E> viewClass) {
        Preconditions.checkState(entityType != null, "no entity type specified");
        return new EntityQuery<>(entityType, filter.build(), sortKey.build(), viewClass);
    }
}
