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
