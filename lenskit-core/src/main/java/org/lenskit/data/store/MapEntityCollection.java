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
