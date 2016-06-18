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
import com.google.common.collect.Ordering;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.GroupingObjectStream;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import java.util.Arrays;
import java.util.Collections;
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
        ObjectStream<E> stream =
                ObjectStreams.transform(ObjectStreams.filter(ObjectStreams.wrap(entities), query),
                                        Entities.projection(query.getViewType()));
        List<SortKey> sort = query.getSortKeys();
        if (sort.isEmpty()) {
            return stream;
        }

        // we must sort
        List<E> list = ObjectStreams.makeList(stream);
        Ordering<Entity> ord = null;
        for (SortKey k: sort) {
            if (ord == null) {
                ord = k.ordering();
            } else {
                ord = ord.compound(k.ordering());
            }
        }
        Collections.sort(list, ord);
        return ObjectStreams.wrap(list);
    }

    @Override
    public <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> query, TypedName<Long> grpCol) {
        EntityQueryBuilder qb = EntityQuery.newBuilder();
        qb.setEntityType(query.getEntityType())
          .addFilterFields(query.getFilterFields());
        qb.addSortKey(grpCol);
        qb.addSortKeys(query.getSortKeys());
        ObjectStream<E> stream = streamEntities(qb.buildWithView(query.getViewType()));
        return new GroupStream<>(stream, grpCol);
    }

    private static class GroupStream<E extends Entity> extends GroupingObjectStream<IdBox<List<E>>, E> {
        private final TypedName<Long> attribute;
        private long id;
        private ImmutableList.Builder<E> builder;

        GroupStream(@WillCloseWhenClosed ObjectStream<E> base, TypedName<Long> attr) {
            super(base);
            attribute = attr;
        }

        @Override
        protected void clearGroup() {
            builder = null;
        }

        @Override
        protected boolean handleItem(@Nonnull E item) {
            if (builder == null) {
                id = item.getLong(attribute);
                builder = ImmutableList.builder();
            } else if (id != item.getLong(attribute)) {
                return false;
            }
            builder.add(item);
            return true;
        }

        @Nonnull
        @Override
        protected IdBox<List<E>> finishGroup() {
            IdBox<List<E>> box = IdBox.create(id, (List<E>) builder.build());
            builder = null;
            return box;
        }
    }
}
