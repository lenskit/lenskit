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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * Base class for data entities in LensKit.  The LensKit data model consists of various entities, each of which has a
 * type, a numeric identifier (as a `long`), and additional data in a key-value mapping.
 *
 * Two entities are equal if their types, IDs, and data are equal.
 *
 * Identifiers are scoped per-type; it is acceptable to LensKit for two entities with different types to have the same
 * identifier, and they are not considered to be the same entity.  However, individual data storage facilities are free
 * to require identifiers to be disjoint.
 */
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
     * Get the names of the fields in this entity.
     */
    Set<String> getAttributeNames();

    /**
     * Get the fields in this entity.
     */
    Set<Attribute<?>> getAttributes();

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
     * @param attribute The attribute to look for.
     * @return `true` if the entity contains `attribute` (same name **and type**).
     */
    boolean hasAttribute(Attribute<?> attribute);

    /**
     * Get the value of an attribute.
     * @param attribute The attribute.
     * @param <T> The attribute's type.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attribute is not present.
     * @throws IllegalArgumentException if a attribute with the same name as `attribute` is present, but it is of an
     *         incompatible type.
     */
    @Nonnull
    <T> T get(Attribute<T> attribute);

    /**
     * Get the value of an attribute by name.
     * @param attr The attribute name.
     * @throws NoSuchAttributeException if the specified attribute is not present.
     * @return The field value, or `null` if the attribute is missing.
     */
    @Nonnull
    Object get(String attr);

    /**
     * Get the value of a possibly-missing attribute.
     * @param attribute The attribute.
     * @param <T> The attribute's type.
     * @return The attribute's value, or `null` if it is not present.
     * @throws IllegalArgumentException if a attribute with the same name as `attribute` is present, but it is of an
     *         incompatible type.
     */
    @Nullable
    <T> T maybeGet(Attribute<T> attribute);

    /**
     * Get the value of a possibly-missing attribute by name.
     * @param attr The attribute name.
     * @return The attribute's value, or `null` if it is not present.
     * @throws IllegalArgumentException if an attribute with the same name as `attr` is present, but it is of an
     *         incompatible type.
     */
    @Nullable
    Object maybeGet(String attr);

    /**
     * Get the value of a attribute that contains a long.
     * @param attribute The attribute.
     * @return The attribute's value.
     * @throws NoSuchAttributeException if the specified attribute is not present.
     * @throws IllegalArgumentException if the attribute is present but its type is not `Long`.
     */
    long getLong(Attribute<Long> attribute);

    /**
     * Get the value of a attr that contains a double.
     * @param attr The attr.
     * @return The attr's value.
     * @throws NoSuchAttributeException if the specified attr is not present.
     * @throws IllegalArgumentException if the attr is present but is not of type `Double`.
     */
    double getDouble(Attribute<Double> attr);
}
