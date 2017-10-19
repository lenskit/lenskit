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
package org.lenskit.data.dao;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * Interface for accessing data in LensKit.
 *
 * LensKit data is represented by *entities*; see {@link org.lenskit.data.entities}
 *
 * DAOs **must** be thread-safe, but individual streams they return need not be.
 */
public interface DataAccessObject {
    /**
     * Get the types of entities that are available in this DAO.
     * @return The set of available entity types.
     */
    Set<EntityType> getEntityTypes();

    /**
     * Get the IDs of all entities with a particular type.
     * @param type The entity type.
     * @return The set of item IDs.
     */
    LongSet getEntityIds(EntityType type);

    /**
     * Look up an entity by ID.
     * @param type The entity type.
     * @param id The entity ID.
     * @return The entity, or `null` if no such entity exists.
     */
    @Nullable
    Entity lookupEntity(EntityType type, long id);

    /**
     * Look up an entity by ID and project it to a view class.
     * @param type The entity type.
     * @param id The entity ID.
     * @param view The view class.
     * @return The entity, or `null` if no such entity exists.
     * @throws IllegalArgumentException if the entity cannot be projected to `view`.
     */
    @Nullable
    <E extends Entity> E lookupEntity(EntityType type, long id, Class<E> view);

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
    <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> query, TypedName<Long> grpCol);

    /**
     * Start a query using the fluent query interface.
     * @param type The type of entity to retrieve.
     * @return The query.
     */
    Query<Entity> query(EntityType type);

    /**
     * Start a query for a particular type using the fluent query interface.  This method looks up the entity type
     * using the {@link org.lenskit.data.entities.DefaultEntityType} annotation on the view class.
     *
     * @param type The view class type.
     * @return The query.
     * @throws IllegalArgumentException if the type has no {@link org.lenskit.data.entities.DefaultEntityType}
     * annotation.
     */
    <V extends Entity> Query<V> query(Class<V> type);
}
