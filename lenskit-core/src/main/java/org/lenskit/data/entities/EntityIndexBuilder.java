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

/**
 * Builder for {@linkplain EntityIndex entity indexes}.  These builders are *destructive*: their {@link #build()}
 * method will destroy the builder's internal state to free memory.
 */
public abstract class EntityIndexBuilder {
    /**
     * Add an entity to the index.
     * @param e The entity to add.
     * @throws IllegalStateException if {@link #build()} has been called.
     */
    public abstract void add(Entity e);

    /**
     * Build the entity index.
     * @return The entity index.
     */
    public abstract EntityIndex build();

    /**
     * Create an entity index builder for an attribute.
     * @param name The attribute.
     * @return The index builder.
     */
    @SuppressWarnings("unchecked")
    public static EntityIndexBuilder create(TypedName<?> name) {
        if (name.getType().equals(Long.class)) {
            return new LongEntityIndexBuilder((TypedName<Long>) name);
        } else {
            return new GenericEntityIndexBuilder(name);
        }
    }
}
