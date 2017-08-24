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
package org.lenskit.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.util.keys.KeyedObject;

import net.jcip.annotations.Immutable;
import java.util.function.Function;

/**
 * A box that associates an object with an ID.
 *
 * @param <T> The type of object contained.
 */
@Immutable
public final class IdBox<T> implements KeyedObject {
    private final long id;
    private final T object;

    /**
     * Construct a new ID box.
     * @param k The ID.
     * @param o The object.
     */
    public IdBox(long k, T o) {
        id = k;
        object = o;
    }

    /**
     * Construct a new ID box.
     * @param id The ID.
     * @param obj The object.
     * @param <T> The object type.
     * @return An ID box associating `obj` with `id`.
     */
    public static <T> IdBox<T> create(long id, T obj) {
        return new IdBox<>(id, obj);
    }

    /**
     * Get the ID associated with the object.
     * @return The ID.
     */
    public long getId() {
        return id;
    }

    @Override
    public long getKey() {
        return getId();
    }

    /**
     * Get the value associated with the object.
     * @return The value.
     */
    public T getValue() {
        return object;
    }

    /**
     * Transform the value in a box.
     * @param function The function to apply.
     * @param <R> The function return type.
     * @return An ID box with the same ID but whose value is the result of applying the function to this box's value.
     */
    public <R> IdBox<R> mapValue(Function<? super T, ? extends R> function) {
        return create(id, function.apply(getValue()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        IdBox<?> idBox = (IdBox<?>) o;

        return new EqualsBuilder()
                .append(id, idBox.id)
                .append(object, idBox.object)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(object)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "IdBox[" + id + ", " + object + "]";
    }
}
