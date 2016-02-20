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

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Attribute;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * A DAO backed by one or more collections of entities.
 */
public class EntityCollectionDAO implements DataAccessObject {
    private final List<Entity> entities;

    public EntityCollectionDAO(List<? extends Entity> data) {
        entities = ImmutableList.copyOf(data);
    }

    public static EntityCollectionDAO create(Entity... data) {
        return new EntityCollectionDAO(Arrays.asList(data));
    }

    @Override
    public LongSet getEntityIds(EntityType type) {
        LongSet ids = new LongOpenHashSet();
        for (Entity e: entities) {
            if (e.getType().equals(type)) {
                ids.add(e.getId());
            }
        }
        return ids;
    }

    @Nullable
    @Override
    public Entity lookupEntity(EntityType type, long id) {
        return Iterables.tryFind(entities,
                                 Predicates.and(Entities.idPredicate(id),
                                                Entities.typePredicate(type)))
                        .orNull();
    }

    @Nullable
    @Override
    public <E extends Entity> E lookupEntity(EntityType type, long id, Class<E> view) {
        Entity entity = lookupEntity(type, id);
        if (entity == null) {
            return null;
        } else {
            return Entities.project(entity, view);
        }
    }

    @Override
    public ObjectStream<Entity> streamEntities(EntityType type) {
        return ObjectStreams.filter(ObjectStreams.wrap(entities),
                                    Entities.typePredicate(type));
    }

    @Override
    public <E extends Entity> ObjectStream<E> streamEntities(EntityQuery<E> query) {
        return ObjectStreams.transform(ObjectStreams.filter(ObjectStreams.wrap(entities), query),
                                       Entities.projection(query.getViewType()));
    }

    @Override
    public <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> query, Attribute<Long> grpCol) {
        // TODO Implement this method
        return null;
    }
}
