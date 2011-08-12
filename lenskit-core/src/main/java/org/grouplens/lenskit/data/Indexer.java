/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

/**
 * Build contiguous 0-based indexes for long IDs.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Indexer implements Index {
    private static final long serialVersionUID = -8383883342128686850L;

    private Long2IntMap indexes;
    private LongArrayList ids;

    /**
     * Construct a new empty indexer.  The first interned ID will have index 0.
     */
    public Indexer() {
        indexes = new Long2IntOpenHashMap();
        indexes.defaultReturnValue(-1);
        ids = new LongArrayList();
    }

    /**
     * Get the ID for an index.
     * @param idx The index to query.
     * @return The ID with index <var>idx</var>.
     * @throws IndexOutOfBoundsException if the index is invalid.
     */
    @Override
    public long getId(int idx) {
        return ids.getLong(idx);
    }

    /**
     * Get the list of all IDs in index order.
     * @return The list of IDs.
     */
    @Override
    public LongList getIds() {
        return LongLists.unmodifiable(ids);
    }

    /**
     * Get the index for an ID.
     * 
     * @param id The ID o query.
     * @return The index assigned to <var>id</var>, or -1 if <var>id</var> has
     *         not been interned.
     */
    @Override
    public int getIndex(long id) {
        return indexes.get(id);
    }

    /**
     * Get the number of objects interned in this indexer.
     */
    @Override
    public int getObjectCount() {
        return ids.size();
    }

    /**
     * Get an index for an ID, generating a new one if necessary.
     * 
     * @param id The ID.
     * @return The index for <var>id</var>. If the ID has already been interned,
     *         the old index is returned; otherwise, a new index is generated
     *         and returned.
     */
    public int internId(long id) {
        int idx = getIndex(id);
        if (idx < 0) {
            idx = ids.size();
            ids.add(id);
            indexes.put(id, idx);
        }
        return idx;
    }
}
