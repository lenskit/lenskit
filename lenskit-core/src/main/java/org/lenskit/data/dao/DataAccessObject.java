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
package org.lenskit.data.dao;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Attribute;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.io.ObjectStream;

/**
 * Interface for accessing data in LensKit.
 *
 * LensKit data is represented by *entities*; see {@link org.lenskit.data.entities}
 */
public interface DataAccessObject {
    /**
     * Get the IDs of all entities with a particular type.
     * @param type The entity type.
     * @return The set of item IDs.
     */
    LongSet getEntityIds(EntityType type);

    /**
     * Stream all entities of a particular type.
     * @param type The entity type.
     * @return The stream of the entities.
     */
    ObjectStream<Entity> streamEntities(EntityType type);

    /**
     * Stream entities in response to a query.
     * @param query The query.
     * @param <E> The view class type.
     * @return The stream of the entities.
     * @throws UnsupportedQueryException if the query cannot be satisfied.
     */
    <E extends Entity> ObjectStream<E> streamEntities(EntityQuery<E> query);

    /**
     * Stream entities in response to a query, grouping them by a long ID.
     *
     * @param query The query.
     * @param grpCol The column to group by.
     * @param <E> The view class type.
     * @return The stream of the entities.
     * @throws UnsupportedQueryException if the query cannot be satisfied
     */
    <E extends Entity> ObjectStream<E> streamEntityGroups(EntityQuery<E> query, Attribute<Long> grpCol);
}
