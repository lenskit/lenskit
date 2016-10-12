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
package org.lenskit.data.entities;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;

/**
 * An attribute associated with an entity, consisting of its name, type, and value.
 * @param <T> The attribute type.
 */
public final class Attribute<T> {
    private final TypedName<T> name;
    private final T value;

    /**
     * Create a new attribute pair.
     * @param name The attribute name.
     * @param val The value.
     */
    private Attribute(@Nonnull TypedName<T> name, @Nonnull T val) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(val, "value");
        Preconditions.checkArgument(name.getType().isInstance(val), "value-type mismatch");
        this.name = name;
        value = val;
    }

    /**
     * Create a new attribute pair.
     * @param name The attribute name.
     * @param val The value.
     * @param <T> The attribute type.
     * @return The attribute-value object.
     */
    public static <T> Attribute<T> create(@Nonnull TypedName<T> name,
                                          @Nonnull T val) {
        return new Attribute<>(name, val);
    }

    /**
     * Get the attribute's typed name.
     * @return The typed name associated with this attribute.
     */
    @Nonnull
    public TypedName<T> getTypedName() {
        return name;
    }

    /**
     * Get the attribute's name.
     * @return The attributge name.
     */
    @Nonnull
    public String getName() {
        return name.getName();
    }

    /**
     * Get the attribute's type.
     * @return The attribute's type.
     */
    @Nonnull
    public Class<T> getType() {
        return name.getType();
    }

    /**
     * Get the attribute's value.
     * @return The attribute value.
     */
    @Nonnull
    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Attribute<?> that = (Attribute<?>) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(value)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("attribute", name)
                .append("value", value)
                .toString();
    }
}
