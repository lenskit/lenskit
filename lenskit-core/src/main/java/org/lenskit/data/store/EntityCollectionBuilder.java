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

import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;

/**
 * Created by MichaelEkstrand on 4/18/2017.
 */
public abstract class EntityCollectionBuilder {
    /**
     * Add an index to an entity collection to speed up lookups.
     * @param attribute The attribute to index.
     * @param <T> The attribute type
     * @return The builder (for chaining).
     */
    public abstract <T> EntityCollectionBuilder addIndex(TypedName<T> attribute);

    /**
     * Add an index to an entity collection to speed up lookups.
     * @param attrName The name of the attribute to index.
     * @return The builder (for chaining).
     */
    public abstract EntityCollectionBuilder addIndex(String attrName);

    /**
     * Add an entity to the collection.  If an entity with the specified ID already exists,
     * it is replaced.
     * @param e The entity to add.
     * @return The builder (for chaining).
     */
    public EntityCollectionBuilder add(Entity e) {
        return add(e, true);
    }

    /**
     * Add an entity to the collection.
     * @param e The entity to add.
     * @param replace Whether to replace. If `false`, and an entity with the same ID as `e` has already been added,
     *                this entity is **silently** ignored.
     * @return The builder (for chaining).
     */
    public abstract EntityCollectionBuilder add(Entity e, boolean replace);

    /**
     * Get a view of the entities added, for iteration and re-processing.
     * @return The view of entities added.
     */
    public abstract Iterable<Entity> entities();

    /**
     * Build the entity collection.
     * @return The collection of entities.
     */
    public abstract EntityCollection build();
}
