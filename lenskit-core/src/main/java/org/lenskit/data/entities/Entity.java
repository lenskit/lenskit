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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Base class for data entities in LensKit.  The LensKit data model consists of various entities, each of which has a
 * type, a numeric identifier (as a `long`), and additional data (attributes) in a key-value mapping.
 *
 * Two entities are equal if their types, IDs, and data are equal.
 *
 * Identifiers are scoped per-type; it is acceptable to LensKit for two entities with different types to have the same
 * identifier, and they are not considered to be the same entity.
 *
 * Attributes are identified by name; for type safety, the are often accessed using {@linkplain TypedName typed names},
 * such as those defined in {@link CommonAttributes}.  Attribute values cannot be null; if you must safely handle the
 * absence of an attribute, test with {@link #hasAttribute(TypedName)} or use {@link #maybeGet(TypedName)}.
 */
@Immutable
@BuiltBy(BasicEntityBuilder.class)
public interface Entity {
    /**
     * Get the identifier of this entity.
     *
     * @return The entity identifier.
     */
    long getId();

    /**
     * Get the type of this entity.
     *
     * @return The entity's type.
     */
    EntityType getType();

    /**
     * Get the names of the attributes in this entity.
     */
    Set<String> getAttributeNames();

    /**
     * Get the attributes in this entity.
     */
    Set<TypedName<?>> getTypedAttributeNames();

    /**
     * Get the attribute-value pairs.
     */
    Collection<Attribute<?>> getAttributes();

    /**
     * View this entity as a map.
     * @return A map reflecting the entity's data.
     */
    Map<String,Object> asMap();

    /**
     * Check if the entity has a field with a particular name.
     *
     * @param name The field name to look for.
     * @return `true` if the entity has a field named `name`.
     */
    boolean hasAttribute(String name);

    /**
     * Check if the entity has a attribute.
     *
     * @param name The attribute name to look for.
     * @return `true` if the entity contains `attribute` and the value is of the associated type.
     */
    boolean hasAttribute(TypedName<?> name);

    /**
     * Get the value of an attribute.
     * @param name The attribute name.
     * @param <T> The attribute's type.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attribute is not present.
     * @throws IllegalArgumentException if a attribute with the same name as `attribute` is present, but it is of an
     *         incompatible type.
     */
    @Nonnull
    <T> T get(TypedName<T> name);

    /**
     * Get the value of an attribute by name.
     * @param attr The attribute name.
     * @throws NoSuchAttributeException if the specified attribute is not present.
     * @return The attribute value.
     */
    @Nonnull
    Object get(String attr);

    /**
     * Get the value of a possibly-missing attribute.
     * @param name The attribute name.
     * @param <T> The attribute's type.
     * @return The attribute's value, or `null` if it is not present.
     * @throws IllegalArgumentException if a attribute with the same name as `attribute` is present, but it is of an
     *         incompatible type.
     */
    @Nullable
    <T> T maybeGet(TypedName<T> name);

    /**
     * Get the value of a possibly-missing attribute by name.
     * @param attr The attribute name.
     * @return The attribute's value, or `null` if it is not present.
     */
    @Nullable
    Object maybeGet(String attr);

    /**
     * Get the value of a attribute that contains a long.
     * @param name The attribute name.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attribute is not present.
     * @throws IllegalArgumentException if the attribute is present but its type is not `Long`.
     */
    long getLong(TypedName<Long> name);

    /**
     * Get the value of a attr that contains a double.
     * @param name The attribute name.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attr is not present.
     * @throws IllegalArgumentException if the attr is present but is not of type `Double`.
     */
    double getDouble(TypedName<Double> name);

    /**
     * Get the value of a attr that contains a int.
     * @param name The attribute name.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attr is not present.
     * @throws IllegalArgumentException if the attr is present but is not of type `Integer`.
     */
    int getInteger(TypedName<Integer> name);

    /**
     * Get the value of a attr that contains a boolean.
     * @param name The attribute name.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attr is not present.
     * @throws IllegalArgumentException if the attr is present but is not of type `Boolean`.
     */
    boolean getBoolean(TypedName<Boolean> name);
}
