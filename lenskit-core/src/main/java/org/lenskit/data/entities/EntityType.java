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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A type for an entity.  Obtain an entity from a named type with {@link #forName(String)}.
 *
 * Type names are *case-insensitive*, and are normalized to lowercase in the {@linkplain Locale#ROOT root locale}.
 */
public final class EntityType implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Map<String,EntityType> TYPE_CACHE = new HashMap<>();

    private final String name;

    private EntityType(String n) {
        name = n;
    }

    /**
     * Get the name of this entity type.
     * @return The entity type's name.
     */
    @JsonValue
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "EntityType[" + name + "]";
    }

    private Object readResolve() {
        // Look up the name to ensure singletons
        // This is not entirely safe; see Effective Java, 2nd Ed., #77 for details.
        // However, we do not depend on singletons for security, only for correctness.
        // It is acceptable if malicious serialization streams result in broken objects.
        return forName(name);
    }

    /**
     * Get the entity type for a name.
     * @param name The type name.
     * @return The entity type with name `name`.
     */
    @Nonnull
    @JsonCreator
    public static synchronized EntityType forName(String name) {
        String normedName = name.toLowerCase(Locale.ROOT);
        EntityType type = TYPE_CACHE.get(normedName);
        if (type == null) {
            type = new EntityType(normedName);
            TYPE_CACHE.put(normedName, type);
        }
        return type;
    }
}
