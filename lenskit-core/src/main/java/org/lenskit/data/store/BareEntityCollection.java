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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.*;
import org.lenskit.util.keys.LongSortedArraySet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A bare entity collection that stores ID-only entities.
 */
class BareEntityCollection extends EntityCollection {
    private final EntityType entityType;
    private final LongSortedArraySet idSet;

    BareEntityCollection(EntityType et, LongSortedArraySet ids) {
        entityType = et;
        idSet = ids;
    }

    @Override
    public EntityType getType() {
        return entityType;
    }

    @Override
    public LongSet idSet() {
        return idSet;
    }

    @Nullable
    @Override
    public Entity lookup(long id) {
        if (idSet.contains(id)) {
            return Entities.create(entityType, id);
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public <T> List<Entity> find(TypedName<T> name, T value) {
        if (name == CommonAttributes.ENTITY_ID) {
            Entity e = lookup((Long) value);
            if (e != null) {
                return Collections.singletonList(e);
            }
        }

        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public <T> List<Entity> find(Attribute<T> attr) {
        return find(attr.getName(), attr.getValue());
    }

    @Nonnull
    @Override
    public List<Entity> find(String name, Object value) {
        if (value instanceof Long) {
            return find(TypedName.create(name, Long.class), (Long) value);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Iterator<Entity> iterator() {
        return new EntityIterator(entityType, idSet.iterator());
    }

    @Override
    public int size() {
        return idSet.size();
    }

    static class EntityIterator implements Iterator<Entity> {
        private final EntityType type;
        private final LongIterator idIter;

        EntityIterator(EntityType et, LongIterator iter) {
            type = et;
            idIter = iter;
        }

        @Override
        public boolean hasNext() {
            return idIter.hasNext();
        }

        @Override
        public Entity next() {
            return Entities.create(type, idIter.nextLong());
        }
    }
}
