/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.indexes;

import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;
import java.util.Collection;

/**
 * Bidirectional mapping between long IDs and consecutive, 0-based integer indexes.  This makes
 * it easier to store information about sparse IDs (e.g. user or item IDs) in dense, indexed
 * data structures such as arrays, matrices, or lists.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class IdIndexMapping implements Serializable {
    /**
     * Get the index of an ID.
     *
     * @param id The ID to query.
     * @return The ID's index or a negative value if the ID does not exist.
     * @throws IllegalArgumentException if the ID is not in the index.
     */
    public int getIndex(long id) {
        int idx = tryGetIndex(id);
        if (idx < 0) {
            throw new IllegalArgumentException("id " + id + " not in index");
        }
        return idx;
    }

    /**
     * Query whether this index contains a particular ID.
     * @param id The ID to look for.
     * @return {@code true} if the index contains the ID.
     */
    public boolean containsId(long id) {
        return tryGetIndex(id) >= 0;
    }

    /**
     * Try to get the index for an ID, returning a negative value if it does not exist.
     * This method is like {@link #getIndex(long)}, except it returns a negative value
     * instead of throwing an exception if the id does not exist.
     * @param id The ID to look for.
     * @return The index of the ID, or a negative value if it is not in the index.
     */
    public abstract int tryGetIndex(long id);

    /**
     * Get the ID for an index.
     *
     * @param idx The index of the ID to retrieve.
     * @return The ID for the given {@var idx}.
     * @throws IndexOutOfBoundsException if {@code idx} is not a valid index.
     */
    public abstract long getId(int idx);

    /**
     * Get the size of this index.
     *
     * @return The number of indexed IDs.
     */
    public abstract int size();

    /**
     * Get the list of indexed IDs.
     *
     * @return The list of IDs in the index.  No ID will appear twice.
     */
    public abstract LongList getIdList();

    public static IdIndexMapping create(Collection<? extends Long> ids) {
        IdIndexMappingBuilder bld = new IdIndexMappingBuilder();
        bld.addAll(ids);
        return bld.build();
    }
}
