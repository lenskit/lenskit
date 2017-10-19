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
