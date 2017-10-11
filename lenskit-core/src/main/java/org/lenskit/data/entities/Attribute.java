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
package org.lenskit.data.entities;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
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
        Preconditions.checkArgument(name.getRawType().isInstance(val),
                                    "creating attribute %s: value '%s' is not of correct type",
                                    name, val);
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
    public TypeToken<T> getType() {
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
