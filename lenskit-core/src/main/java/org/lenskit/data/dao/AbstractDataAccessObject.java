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
package org.lenskit.data.dao;

import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.DefaultEntityType;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.GroupingObjectStream;
import org.lenskit.util.io.ObjectStream;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.util.List;

/**
 * Helper class to make it easier to create DAOs.  This base class implements several of the DAO
 * convenience methods in terms of the master {@link #streamEntities(EntityQuery)} method, so that
 * DAO implementers only need to translate {@link EntityQuery} objects into their underlying query
 * framework (collection predicates, Jooq queries, HQL, or whatever).
 */
public abstract class AbstractDataAccessObject implements DataAccessObject {
    @Override
    public Query<Entity> query(EntityType type) {
        return new JavaQuery<>(this, type, Entity.class);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation uses an internal {@link Query} implementation to prepare queries for
     * {@link #streamEntities(EntityQuery)} and {@link #streamEntityGroups(EntityQuery, TypedName)}.
     */
    @Override
    public <V extends Entity> Query<V> query(Class<V> type) {
        DefaultEntityType det = type.getAnnotation(DefaultEntityType.class);
        if (det == null) {
            throw new IllegalArgumentException(type + " has no default entity type annotation");
        }
        return new JavaQuery<>(this, EntityType.forName(det.value()), type);
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #streamEntities(EntityQuery)}
     */
    @Override
    public ObjectStream<Entity> streamEntities(EntityType type) {
        return streamEntities(EntityQuery.newBuilder(type).build());
    }

    /**
     * {@inheritDoc}
     *
     * This implementation delegates to {@link #streamEntities(EntityType)} and groups the result.
     */
    @Override
    public <E extends Entity> ObjectStream<IdBox<List<E>>> streamEntityGroups(EntityQuery<E> query, TypedName<Long> grpCol) {
        EntityQueryBuilder qb = EntityQuery.newBuilder(query.getEntityType());
        qb.addFilterFields(query.getFilterFields());
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
