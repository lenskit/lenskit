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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.entities.*;
import org.lenskit.util.describe.Describable;
import org.lenskit.util.describe.DescriptionWriter;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class MapEntityCollection extends EntityCollection implements Serializable, Describable {
    private static long serialVersionUID = 1L;
    private final EntityType type;
    private final KeyedObjectMap<Entity> store;
    private final Map<String, EntityIndex> indexes;
    private final String contentHash;

    MapEntityCollection(EntityType type, KeyedObjectMap<Entity> entities, Map<String,EntityIndex> idxes, HashCode hash) {
        this.type = type;
        store = entities;
        indexes = idxes;
        contentHash = hash.toString();
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public LongSet idSet() {
        return store.keySet();
    }

    @Override
    @Nullable
    public Entity lookup(long id) {
        return store.get(id);
    }

    @Override
    @Nonnull
    public <T> List<Entity> find(TypedName<T> name, T value) {
        return find(name.getName(), value);
    }

    @Override
    @Nonnull
    public <T> List<Entity> find(Attribute<T> attr) {
        return find(attr.getTypedName(), attr.getValue());
    }

    @Override
    @Nonnull
    public List<Entity> find(String name, Object value) {
        Preconditions.checkNotNull(name, "attribute name");
        Preconditions.checkNotNull(value, "attribute value");

        EntityIndex index = indexes.get(name);
        if (index != null) {
            return index.getEntities(value);
        }

        // no index, go ahead and search
        ImmutableList.Builder<Entity> results = ImmutableList.builder();
        for (Entity e: store) {
            if (value.equals(e.maybeGet(name))) {
                results.add(e);
            }
        }
        return results.build();
    }

    @Override
    public Map<Long,List<Entity>> grouped(TypedName<Long> attr) {
        Preconditions.checkArgument(attr != CommonAttributes.ENTITY_ID,
                                    "cannot group by entity ID");
        EntityIndex idx = indexes.get(attr.getName());
        if (idx == null) {
            return store.values()
                        .stream()
                        .filter(e -> e.hasAttribute(attr))
                        .collect(Collectors.groupingBy(e -> e.getLong(attr)));
        } else {
            return idx.getValues()
                      .stream()
                      .collect(Collectors.toMap(l -> (Long) l,
                                                idx::getEntities));
        }
    }

    @Nonnull
    @Override
    public Iterator<Entity> iterator() {
        return store.iterator();
    }

    @Override
    public int size() {
        return store.size();
    }

    @Override
    public void describeTo(DescriptionWriter writer) {
        writer.putField("size", store.size());
        writer.putField("contentHash", contentHash);
    }
}
