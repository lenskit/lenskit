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
package org.lenskit.data.dao.file;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Entity source backed by a collection.
 */
class CollectionEntitySource implements EntitySource, Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final ImmutableList<Entity> entities;
    private final ImmutableSet<EntityType> types;
    private final ImmutableMap<String, Object> metadata;

    /**
     * Construct a new collection entity source.
     * @param n The source name.
     * @param es The entities.
     */
    public CollectionEntitySource(String n, Collection<? extends Entity> es, Map<String,Object> meta) {
        name = n;
        entities = ImmutableList.copyOf(es);
        ImmutableSet.Builder<EntityType> tb = ImmutableSet.builder();
        for (Entity e: entities) {
            tb.add(e.getType());
        }
        types = tb.build();
        metadata = ImmutableMap.copyOf(meta);
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Set<EntityType> getTypes() {
        return types;
    }

    @Nullable
    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public ObjectStream<Entity> openStream() throws IOException {
        return ObjectStreams.wrap(entities);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
