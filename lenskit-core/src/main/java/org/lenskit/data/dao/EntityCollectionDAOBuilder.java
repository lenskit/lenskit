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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.lenskit.data.entities.*;
import org.lenskit.data.store.EntityCollection;
import org.lenskit.data.store.EntityCollectionBuilder;

import net.jcip.annotations.NotThreadSafe;
import java.util.*;

/**
 * Builder for entity collection DAOs.  These builders are *destructive*: their {@link #build()} method cannot be called
 * more than once.
 */
@NotThreadSafe
public class EntityCollectionDAOBuilder {
    private List<TypedName<Long>> defaultIndexes = new ArrayList<>();
    private Map<EntityType, EntityCollectionBuilder> entitySets = new IdentityHashMap<>();
    // remember the last builder used as a fast path
    private EntityCollectionBuilder lastBuilder = null;
    private EntityType last = null;

    /**
     * Set a layout for an entity type.  A layout limits the possible attributes of entities of that type, but can
     * result in more efficient storage.
     * @param et The entity type.
     * @param attributes The set of known attributes.
     * @return The builder (for chaining).
     * @throws IllegalStateException if the specified entity type already has a layout or entities.
     */
    public EntityCollectionDAOBuilder addEntityLayout(EntityType et, AttributeSet attributes) {
        return addEntityLayout(et, attributes, null);
    }

    /**
     * Set a layout for an entity type.  A layout limits the possible attributes of entities of that type, but can
     * result in more efficient storage.
     * @param et The entity type.
     * @param attributes The set of known attributes.
     * @param ebc The entity builder class for reconstituting entities.
     * @return The builder (for chaining).
     * @throws IllegalStateException if the specified entity type already has a layout or entities.
     */
    public EntityCollectionDAOBuilder addEntityLayout(EntityType et, AttributeSet attributes, Class<? extends EntityBuilder> ebc) {
        if (entitySets.containsKey(et)) {
            throw new IllegalStateException("layout or entities already added for " + et);
        }
        EntityCollectionBuilder ecb = EntityCollection.newBuilder(et, attributes, ebc);
        for (TypedName<?> name: defaultIndexes) {
            ecb.addIndex(name);
        }
        entitySets.put(et, ecb);
        return this;
    }

    /**
     * Index entities by an attribute.
     * @param et The entity type.
     * @param attr The attribute to index.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder addIndex(EntityType et, TypedName<?> attr) {
        Preconditions.checkState(entitySets != null, "build() already called");
        EntityCollectionBuilder builder = findBuilder(et);
        builder.addIndex(attr);
        return this;
    }

    /**
     * Add an attribute to index by default on all entities.
     * @param attr The attribute to index.
     */
    public void addDefaultIndex(TypedName<Long> attr) {
        defaultIndexes.add(attr);
        for (EntityCollectionBuilder ecb: entitySets.values()) {
            ecb.addIndex(attr);
        }
    }

    /**
     * Get the entity types registered with this builder so far.
     * @return The entity types registered so far.
     */
    public Set<EntityType> getEntityTypes() {
        return ImmutableSet.copyOf(entitySets.keySet());
    }

    /**
     * Add an entity to the DAO.
     * @param e The entity to add
     * @return The DAO builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntity(Entity e) {
        Preconditions.checkState(entitySets != null, "build() already called");
        EntityType type = e.getType();
        EntityCollectionBuilder bld = findBuilder(type);
        bld.add(e);

        return this;
    }

    private EntityCollectionBuilder findBuilder(EntityType type) {
        if (type != last) {
            lastBuilder = entitySets.get(type);
            last = type;
            if (lastBuilder == null) {
                lastBuilder = EntityCollection.newBuilder(type);
                for (TypedName<?> name: defaultIndexes) {
                    lastBuilder.addIndex(name);
                }
                entitySets.put(type, lastBuilder);
            }
        }
        assert lastBuilder != null;
        return lastBuilder;
    }

    /**
     * Add multiple entities.
     * @param entities The entity list.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntities(Entity... entities) {
        return addEntities(Arrays.asList(entities));
    }

    /**
     * Add multiple entities.
     * @param entities The entity list.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder addEntities(Iterable<? extends Entity> entities) {
        for (Entity e: entities) {
            addEntity(e);
        }

        return this;
    }

    /**
     * Derive bare entities from the values in another type of entity.  This method only consults the entities added
     * so far, so it should be called *after* all other calls to {@link #addEntity(Entity)} and friends.  If an entity
     * has already been added with the same type and ID as one of the derived entities, it is kept instead of the derived
     * entity.
     *
     * @param derived The derived entity type.
     * @param source The source entity type.
     * @param attr The source attribute.
     * @return The builder (for chaining).
     */
    public EntityCollectionDAOBuilder deriveEntities(EntityType derived, EntityType source, TypedName<Long> attr) {
        EntityCollectionBuilder src = entitySets.get(source);
        if (src == null) {
            // no source entities, skip
            return this;
        }

        EntityCollectionBuilder ecb = entitySets.get(derived);
        if (ecb == null) {
            ecb = EntityCollection.newBareBuilder(derived);
            entitySets.put(derived, ecb);
        }
        for (Entity e: src.entities()) {
            if (e.hasAttribute(attr)) {
                long key = e.getLong(attr);
                ecb.add(Entities.create(derived, key), false);
            }
        }

        return this;
    }

    public EntityCollectionDAO build() {
        Preconditions.checkState(entitySets != null, "build() already called");
        ImmutableMap.Builder<EntityType, EntityCollection> mb = ImmutableMap.builder();
        for (Map.Entry<EntityType, EntityCollectionBuilder> e: entitySets.entrySet()) {
            mb.put(e.getKey(), e.getValue().build());
        }

        entitySets = null;

        return new EntityCollectionDAO(mb.build());
    }
}
