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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lenskit.util.IdBox;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;

/**
 * A generic entity index builder.
 */
class LongEntityIndexBuilder extends EntityIndexBuilder {
    private final TypedName<Long> attributeName;
    private Long2ObjectMap<ImmutableList.Builder<Entity>> entityLists;

    /**
     * Construct a new index builder.
     * @param name The attribute name to index.
     */
    LongEntityIndexBuilder(String name) {
        this(TypedName.create(name, Long.class));
    }

    /**
     * Construct a new index builder.
     * @param name The attribute name to index.
     */
    LongEntityIndexBuilder(TypedName<Long> name) {
        attributeName = name;
        entityLists = new Long2ObjectOpenHashMap<>();
    }

    @Override
    public void add(Entity e) {
        Preconditions.checkState(entityLists != null, "build() already called");
        if (e.hasAttribute(attributeName)) {
            long value = e.getLong(attributeName);
            ImmutableList.Builder<Entity> lb = entityLists.get(value);
            if (lb == null) {
                lb = ImmutableList.builder();
                entityLists.put(value, lb);
            }
            lb.add(e);
        }
    }

    @Override
    public EntityIndex build() {
        Preconditions.checkState(entityLists != null, "build() already called");
        // arrange compact storage of the index
        KeyedObjectMapBuilder<IdBox<ImmutableList<Entity>>> bld = KeyedObjectMap.newBuilder();
        for (Long2ObjectMap.Entry<ImmutableList.Builder<Entity>> entry: entityLists.long2ObjectEntrySet()) {
            long value = entry.getLongKey();
            bld.add(IdBox.create(value, entry.getValue().build()));
            entry.setValue(null);
        }
        entityLists = null;
        return new LongEntityIndex(bld.build());
    }
}
