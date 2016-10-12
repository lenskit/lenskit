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

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * A sort key describing a sort operation.
 */
public final class SortKey {
    private final TypedName<?> attribute;
    private final SortOrder order;
    private final Comparator<?> comparator;

    /**
     * Create a new sort key in ascending order.
     * @param attr The attribute.
     */
    SortKey(TypedName<? extends Comparable> attr) {
        this(attr, SortOrder.ASCENDING);
    }

    /**
     * Create a new sort key.
     * @param attr The attribute.
     * @param ord The sort order.
     */
    SortKey(TypedName<? extends Comparable> attr, SortOrder ord) {
        attribute = attr;
        order = ord;
        comparator = null;
    }

    /**
     * Create a new sort key.
     * @param attr The attribute.
     * @param comp A comparator to use.
     */
    private SortKey(TypedName<?> attr, Comparator<?> comp) {
        attribute = attr;
        comparator = comp;
        order = null;
    }

    /**
     * Create a new sort key in ascending order.
     * @param attr The attribute.
     */
    public static SortKey create(TypedName<? extends Comparable> attr) {
        return new SortKey(attr, SortOrder.ASCENDING);
    }

    /**
     * Create a new sort key.
     * @param attr The attribute.
     * @param ord The sort order.
     */
    public static SortKey create(TypedName<? extends Comparable> attr, SortOrder ord) {
        return new SortKey(attr, ord);
    }

    /**
     * Create a new sort key with a value comparator.
     * @param attr The attribute.
     * @param comp A comparator to use.
     */
    public static <T> SortKey create(TypedName<T> attr, Comparator<? super T> comp) {
        return new SortKey(attr, comp);
    }

    /**
     * Get the attribute to sort by.
     * @return The attribute to sort by.
     */
    public TypedName<?> getAttribute() {
        return attribute;
    }

    /**
     * Get the sort order.
     * @return The sort order.
     */
    @Nullable
    public SortOrder getOrder() {
        return order;
    }

    /**
     * Query whether this sort key has a custom comparator.
     * @return The custom value comparator.
     */
    public boolean hasCustomComparator() {
        return comparator != null;
    }

    /**
     * Create an ordering (comparator) over entities from this sort key.
     * @return An ordering over entities.
     */
    @SuppressWarnings("unchecked")
    public Ordering<Entity> ordering() {
        Ordering<Entity> ord;
        if (comparator != null) {
            ord = Ordering.from(comparator)
                          .onResultOf((Function) Entities.attributeValueFunction(attribute));
        } else {
            ord = Ordering.natural()
                          .onResultOf((Function) Entities.attributeValueFunction(attribute));
        }
        if (SortOrder.DESCENDING.equals(order)) {
            ord = ord.reverse();
        }
        return ord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SortKey sortKey = (SortKey) o;

        return new EqualsBuilder()
                .append(attribute, sortKey.attribute)
                .append(order, sortKey.order)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(attribute)
                .append(order)
                .toHashCode();
    }
}
