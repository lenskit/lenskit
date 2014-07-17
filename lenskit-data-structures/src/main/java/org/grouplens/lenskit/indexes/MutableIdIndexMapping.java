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

import it.unimi.dsi.fastutil.longs.*;

/**
 * Mutable index mapping. Use this when you need to have indexes before you've seen all the IDs.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class MutableIdIndexMapping extends IdIndexMapping {
    private static final long serialVersionUID = 1L;

    private Long2IntMap indexes;
    private LongArrayList ids;

    /**
     * Construct a new empty indexer.  The first interned ID will have index 0.
     */
    public MutableIdIndexMapping() {
        indexes = new Long2IntOpenHashMap();
        indexes.defaultReturnValue(-1);
        ids = new LongArrayList();
    }

    @Override
    public long getId(int idx) {
        return ids.getLong(idx);
    }

    @Override
    public LongList getIdList() {
        return LongLists.unmodifiable(ids);
    }

    @Override
    public int tryGetIndex(long id) {
        return indexes.get(id);
    }

    @Override
    public int size() {
        return ids.size();
    }

    /**
     * Get an index for an ID, generating a new one if necessary.
     *
     * @param id The ID.
     * @return The index for {@var id}. If the ID has already been added to the index,
     *         the old index is returned; otherwise, a new index is generated and returned.
     */
    public int internId(long id) {
        int idx = tryGetIndex(id);
        if (idx < 0) {
            idx = ids.size();
            ids.add(id);
            indexes.put(id, idx);
        }
        return idx;
    }

    /**
     * Make an immutable copy of this index mapping.
     *
     * @return An immutable copy of the index mapping.
     */
    public IdIndexMapping immutableCopy() {
        return new ImmutableHashIdIndexMapping(indexes);
    }
}
