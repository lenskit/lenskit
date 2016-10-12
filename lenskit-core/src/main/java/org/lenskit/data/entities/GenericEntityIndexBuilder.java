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
import com.google.common.collect.ImmutableListMultimap;

/**
 * A generic entity index builder.
 */
class GenericEntityIndexBuilder extends EntityIndexBuilder {
    private final TypedName<?> attributeName;
    private ImmutableListMultimap.Builder<Object,Entity> builder;

    /**
     * Construct a new index builder.
     * @param name The attribute name to index.
     */
    GenericEntityIndexBuilder(String name) {
        this(TypedName.create(name, Object.class));
    }

    /**
     * Construct a new index builder.
     * @param name The attribute name to index.
     */
    GenericEntityIndexBuilder(TypedName<?> name) {
        attributeName = name;
        builder = ImmutableListMultimap.builder();
    }

    @Override
    public void add(Entity e) {
        Preconditions.checkState(builder != null, "build() already called");
        Object value = e.maybeGet(attributeName);
        if (value != null) {
            builder.put(value, e);
        }
    }

    @Override
    public EntityIndex build() {
        Preconditions.checkState(builder != null, "build() already called");
        ImmutableListMultimap<Object, Entity> data = builder.build();
        builder = null;
        return new GenericEntityIndex(data);
    }
}
