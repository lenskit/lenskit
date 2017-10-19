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
package org.lenskit.data.dao;

import com.google.common.collect.ImmutableList;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;

import java.util.List;

/**
 * Grouped query interface.
 */
public class GroupedQuery<E extends Entity> {
    private final DataAccessObject dao;
    private final EntityQuery<E> query;
    private final TypedName<Long> attribute;

    GroupedQuery(DataAccessObject dao, EntityQuery<E> query, TypedName<Long> attribute) {
        this.dao = dao;
        this.query = query;
        this.attribute = attribute;
    }

    /**
     * Stream the groups of entities.
     * @return The stream of groups.
     */
    public ObjectStream<IdBox<List<E>>> stream() {
        return dao.streamEntityGroups(query, attribute);
    }

    /**
     * Get the grouped entities as a list of groups.
     * @return The list of groups.
     */
    public List<IdBox<List<E>>> get() {
        try (ObjectStream<IdBox<List<E>>> stream = stream()) {
            return ImmutableList.copyOf(stream);
        }
    }
}
