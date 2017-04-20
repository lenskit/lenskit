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
package org.lenskit.data.store;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.dao.SortKey;
import org.lenskit.data.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.List;

/**
 * A collection of entities of a single type.  This collection augments the `Collection` interface with logic for
 * different kinds of (possibly optimized) entity searches.
 */
public abstract class EntityCollection extends AbstractCollection<Entity> {
    EntityCollection() {}

    /**
     * Create a new entity collection builder.
     * @return The builder.
     */
    public static EntityCollectionBuilder newBuilder(EntityType type) {
        return new MapEntityCollectionBuilder(type);
    }

    /**
     * Create a new packed entity collection builder.
     * @param type The entity type.
     * @param attrs The attributes to store.
     * @return An entity collection builder.
     */
    public static EntityCollectionBuilder newBuilder(EntityType type, AttributeSet attrs) {
        return new PackedEntityCollectionBuilder(type, attrs);
    }

    /**
     * Get the type of entity stored in this collection.
     * @return The entity type this collection stores.
     */
    public abstract EntityType getType();

    public abstract LongSet idSet();

    /**
     * Look up an entity by ID.
     * @param id The entity ID.
     * @return The entity, or `null` if no such entity exists.
     */
    @Nullable
    public abstract Entity lookup(long id);

    /**
     * Find entities by attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return A list of entities for which attribute `name` has value `value`.
     */
    @Nonnull
    public abstract <T> List<Entity> find(TypedName<T> name, T value);

    /**
     * Find entities with an attribute.
     * @param attr The attribute to look for.
     * @param <T> The attribute type.
     * @return A list of entities with the specified attribute.
     */
    @Nonnull
    public abstract <T> List<Entity> find(Attribute<T> attr);

    /**
     * Find entities by attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return A list of entities for which attribute `name` has value `value`.
     */
    @Nonnull
    public abstract List<Entity> find(String name, Object value);

    /**
     * Get the sort keys, if this collection stores attributes in sorted order.
     * @return The sort keys.
     */
    public List<SortKey> getSortKeys() {
        return ImmutableList.of();
    }
}
