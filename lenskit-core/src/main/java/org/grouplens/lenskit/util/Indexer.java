/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.VectorEntry.State;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;

/**
 * Build contiguous 0-based indexes for long IDs.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @deprecated Use {@link org.grouplens.lenskit.indexes.MutableIdIndexMapping} instead.
 */
@Deprecated
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

    @Override
    public long getId(int idx) {
        return ids.getLong(idx);
    }

    @Override
    public LongList getIds() {
        return LongLists.unmodifiable(ids);
    }

    @Override
    public int getIndex(long id) {
        return indexes.get(id);
    }

    @Override
    public int getObjectCount() {
        return ids.size();
    }

    /**
     * Get an index for an ID, generating a new one if necessary.
     *
     * @param id The ID.
     * @return The index for {@var id}. If the ID has already been interned,
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

    @Override
    public MutableSparseVector convertArrayToVector(double[] values) {
        if(values.length != getObjectCount()){
            throw new IllegalArgumentException("Value array has incorrect length");
        }

        MutableSparseVector newSparseVector = MutableSparseVector.create(ids);
        for(VectorEntry e : newSparseVector.fast(State.EITHER)){
            final int iid = getIndex(e.getKey());
            newSparseVector.set(e, values[iid]);
        }
        return newSparseVector;
    }
}
