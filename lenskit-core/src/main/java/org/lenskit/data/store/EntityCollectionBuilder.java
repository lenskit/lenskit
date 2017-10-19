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
