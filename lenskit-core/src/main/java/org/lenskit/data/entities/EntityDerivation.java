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

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * Describe a derivation of one entity from another.  This is used to extract bare entities from references in
 * other entity types, e.g. user IDs in ratings, so that you don't have to have explicit data source for every
 * type of entity in the system.
 */
public class EntityDerivation {
    private final EntityType type;
    private final EntityType sourceType;
    private final TypedName<Long> attribute;

    private EntityDerivation(EntityType t, EntityType src, TypedName<Long> attr) {
        type = t;
        sourceType = src;
        attribute = attr;
    }

    /**
     * Create a new entity derivation.
     * @param t The derived entity type.
     * @param src The source type.
     * @param attr The attribute to derive from.
     * @return An entity derivation.
     */
    public static EntityDerivation create(EntityType t, EntityType src, TypedName<Long> attr) {
        return new EntityDerivation(t, src, attr);
    }

    /**
     * Get the entity type to be derived.
     * @return The entity type to derive.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Get the source types for the derivation.
     * @return The source types for the derivation.
     */
    public EntityType getSourceType() {
        return sourceType;
    }

    /**
     * Get the source attribute for the derivation.
     * @return The attribute whose value contains IDs of the derived entity.
     */
    public TypedName<Long> getAttribute() {
        return attribute;
    }
}
