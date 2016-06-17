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
package org.lenskit.data.entities;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;

/**
 * An attribute-value association.
 * @param <T> The attribute type.
 */
public final class AttributeValue<T> {
    private final Attribute<T> attribute;
    private final T value;

    /**
     * Create a new attribute-value pair.
     * @param attr The attribute.
     * @param val The value.
     */
    public AttributeValue(@Nonnull Attribute<T> attr, @Nonnull T val) {
        Preconditions.checkNotNull(attr, "attribute");
        Preconditions.checkNotNull(val, "value");
        attribute = attr;
        value = val;
    }

    /**
     * Create a new attribute-value pair.
     * @param attr The attribute.
     * @param val The value.
     * @param <T> The attribute type.
     * @return The attribute-value object.
     */
    public static <T> AttributeValue<T> create(@Nonnull Attribute<T> attr,
                                               @Nonnull T val) {
        return new AttributeValue<>(attr, val);
    }

    /**
     * Get the attribute.
     * @return The attribute associated with this value.
     */
    @Nonnull
    public Attribute<T> getAttribute() {
        return attribute;
    }

    /**
     * Get the value.
     * @return The value associated with the attribute.
     */
    @Nonnull
    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AttributeValue<?> that = (AttributeValue<?>) o;

        return new EqualsBuilder()
                .append(attribute, that.attribute)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(attribute)
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("attribute", attribute)
                .append("value", value)
                .toString();
    }
}
