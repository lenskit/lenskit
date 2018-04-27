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
import com.google.common.collect.ImmutableListMultimap;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;

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
