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

import javax.annotation.Nullable;
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
    Set<String> getFieldNames();

    /**
     * Get the fields in this entity.
     */
    Set<Field<?>> getFields();

    /**
     * Check if the entity has a field with a particular name.
     *
     * @param name The field name to look for.
     * @return `true` if the entity has a field named `name`.
     */
    boolean hasField(String name);

    /**
     * Check if the entity has a field.
     *
     * @param field The field to look for.
     * @return `true` if the entity contains `field` (same name **and type**).
     */
    boolean hasField(Field<?> field);

    /**
     * Get the value of a field.
     * @param field The field.
     * @param <T> The field's type.
     * @return The field's value.
     * @throws NoSuchFieldException if the specified field is not present.
     * @throws IllegalArgumentException if a field with the same name as `field` is present, but it is of an
     *         incompatible type.
     */
    @Nullable
    <T> T get(Field<T> field);

    /**
     * Get the value of a field by name.
     * @param field The field name.
     * @throws NoSuchFieldException if the specified field is not present.
     * @return The field value, or `null` if the field is missing.
     */
    @Nullable
    Object get(String field);

    /**
     * Get the value of a possibly-missing field.
     * @param field The field.
     * @param <T> The field's type.
     * @return The field's value, or `null` if it is not present.
     * @throws IllegalArgumentException if a field with the same name as `field` is present, but it is of an
     *         incompatible type.
     */
    @Nullable
    <T> T maybeGet(Field<T> field);

    /**
     * Get the value of a possibly-missing field by name.
     * @param field The field name.
     * @return The field's value, or `null` if it is not present.
     * @throws IllegalArgumentException if a field with the same name as `field` is present, but it is of an
     *         incompatible type.
     */
    @Nullable
    Object maybeGet(String field);

    /**
     * Get the value of a field that contains a long.
     * @param field The field.
     * @return The field's value.
     * @throws NoSuchFieldException if the specified field is not present.
     * @throws IllegalArgumentException if the field is present but its type is not `Long`.
     */
    long getLong(Field<Long> field);

    /**
     * Get the value of a field that contains a double.
     * @param field The field.
     * @return The field's value.
     * @throws NoSuchFieldException if the specified field is not present.
     * @throws IllegalArgumentException if the field is present but is not of type `Double`.
     */
    double getDouble(Field<Double> field);
}
