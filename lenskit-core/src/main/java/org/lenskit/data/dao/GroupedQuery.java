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
