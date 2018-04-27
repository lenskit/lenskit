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

import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.Entity;
import org.lenskit.util.IdBox;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Generic implementation of the entity index.
 */
class LongEntityIndex implements EntityIndex {
    private final KeyedObjectMap<IdBox<ImmutableList<Entity>>> entityLists;

    LongEntityIndex(KeyedObjectMap<IdBox<ImmutableList<Entity>>> lists) {
        entityLists = lists;
    }

    @Nonnull
    @Override
    public List<Entity> getEntities(@Nonnull Object value) {
        if (!(value instanceof Long)) {
            return Collections.emptyList();
        }
        long key = (Long) value;
        return getEntities(key);
    }

    @Nonnull
    public List<Entity> getEntities(long key) {
        IdBox<ImmutableList<Entity>> box = entityLists.get(key);
        if (box == null) {
            return Collections.emptyList();
        } else {
            return box.getValue();
        }
    }

    @Override
    public Set<?> getValues() {
        return entityLists.keySet();
    }
}
