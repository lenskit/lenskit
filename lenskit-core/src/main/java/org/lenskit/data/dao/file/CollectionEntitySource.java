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
package org.lenskit.data.dao.file;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nonnull;
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

    @Override
    public ObjectStream<Entity> openStream() throws IOException {
        return ObjectStreams.wrap(entities);
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
