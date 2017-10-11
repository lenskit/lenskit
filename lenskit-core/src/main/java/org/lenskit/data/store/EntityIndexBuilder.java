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

import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;

/**
 * Builder for {@linkplain EntityIndex entity indexes}.  These builders are *destructive*: their {@link #build()}
 * method will destroy the builder's internal state to free memory.
 */
abstract class EntityIndexBuilder {
    /**
     * Add an entity to the index.
     * @param e The entity to add.
     * @throws IllegalStateException if {@link #build()} has been called.
     */
    public abstract void add(Entity e);

    /**
     * Build the entity index.
     * @return The entity index.
     */
    public abstract EntityIndex build();

    /**
     * Create an entity index builder for an attribute.
     * @param name The attribute.
     * @return The index builder.
     */
    @SuppressWarnings("unchecked")
    public static EntityIndexBuilder create(TypedName<?> name) {
        if (name.getRawType().equals(Long.class)) {
            return new LongEntityIndexBuilder((TypedName<Long>) name);
        } else {
            return new GenericEntityIndexBuilder(name);
        }
    }
}
