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

import com.google.common.collect.ImmutableList;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

/**
 * A collection of entities of a single type.  This collection augments the `Collection` interface with logic for
 * different kinds of (possibly optimized) entity searches.
 */
public class EntityCollection extends AbstractCollection<Entity> implements Serializable {
    private static long serialVersionUID = 1L;
    private final EntityType type;
    private final KeyedObjectMap<Entity> store;

    EntityCollection(EntityType type, KeyedObjectMap<Entity> entities) {
        this.type = type;
        store = entities;
    }

    /**
     * Create a new entity collection builder.
     * @return The builder.
     */
    public static EntityCollectionBuilder newBuilder(EntityType type) {
        return new EntityCollectionBuilder(type);
    }

    /**
     * Get the type of entity stored in this collection.
     * @return The entity type this collection stores.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Look up an entity by ID.
     * @param id The entity ID.
     * @return The entity, or `null` if no such entity exists.
     */
    @Nullable
    public Entity lookup(long id) {
        return store.get(id);
    }

    /**
     * Find entities by attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return A list of entities for which attribute `name` has value `value`.
     */
    @Nonnull
    public <T> List<Entity> find(TypedName<T> name, T value) {
        return find(name.getName(), value);
    }

    /**
     * Find entities by attribute.
     * @param name The attribute name.
     * @param value The attribute value; if `null`, returns entities that do not have attribute `name`.
     * @return A list of entities for which attribute `name` has value `value`.
     */
    @Nonnull
    public List<Entity> find(String name, Object value) {
        ImmutableList.Builder<Entity> results = ImmutableList.builder();
        for (Entity e: store) {
            if (value == null) {
                if (!e.hasAttribute(name)) {
                    results.add(e);
                }
            } else {
                if (value.equals(e.maybeGet(name))) {
                    results.add(e);
                }
            }
        }
        return results.build();
    }

    @Nonnull
    @Override
    public Iterator<Entity> iterator() {
        return store.iterator();
    }

    @Override
    public int size() {
        return store.size();
    }
}
