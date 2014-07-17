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
import org.grouplens.lenskit.collections.CollectionUtils;

/**
 * Immutable hash-based implementation index mapping implementation.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
final class ImmutableHashIdIndexMapping extends IdIndexMapping {
    private static final long serialVersionUID = 1L;

    private final Long2IntMap indexes;
    private final long[] ids;

    /**
     * Construct a new empty indexer.  The first interned ID will have index 0.
     */
    public ImmutableHashIdIndexMapping(Long2IntMap map) {
        indexes = new Long2IntOpenHashMap(map);
        indexes.defaultReturnValue(-1);
        ids = new long[indexes.size()];
        for (Long2IntMap.Entry e: CollectionUtils.fast(map.long2IntEntrySet())) {
            ids[e.getIntValue()] = e.getLongKey();
        }
    }

    @Override
    public long getId(int idx) {
        return ids[idx];
    }

    @Override
    public LongList getIdList() {
        return LongLists.unmodifiable(LongArrayList.wrap(ids));
    }

    @Override
    public int tryGetIndex(long id) {
        return indexes.get(id);
    }

    @Override
    public int size() {
        return ids.length;
    }
}
