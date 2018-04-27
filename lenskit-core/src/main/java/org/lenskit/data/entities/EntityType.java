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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.lenskit.inject.Shareable;

import javax.annotation.Nonnull;
import net.jcip.annotations.Immutable;
import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A type for an entity.  Obtain an entity from a named type with {@link #forName(String)}.
 *
 * Entity types are unique objects that can be equality-tested with `==`.
 *
 * Type names are *case-insensitive*, and are normalized to lowercase in the {@linkplain Locale#ROOT root locale}.
 */
@Shareable
@Immutable
public final class EntityType implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final ConcurrentMap<String,EntityType> TYPE_CACHE = new ConcurrentHashMap<>();

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
    public static EntityType forName(String name) {
        String normedName = name.toLowerCase(Locale.ROOT);
        EntityType type = new EntityType(normedName);
        EntityType canonical = TYPE_CACHE.putIfAbsent(normedName, type);
        return canonical == null ? type : canonical;
    }
}
