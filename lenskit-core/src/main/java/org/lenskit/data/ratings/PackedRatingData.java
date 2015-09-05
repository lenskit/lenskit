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
package org.lenskit.data.ratings;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.lenskit.util.keys.KeyIndex;

/**
 * Data storage for packed rating snapshots.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
final class PackedRatingData {
    static final int CHUNK_SHIFT = 12;
    static final int CHUNK_SIZE = 1 << CHUNK_SHIFT;
    static final int CHUNK_MASK = CHUNK_SIZE - 1;

    private final int[][] users;
    private final int[][] items;
    private final double[][] values;
    private final int nprefs;

    private final KeyIndex itemIndex;
    private final KeyIndex userIndex;

    public PackedRatingData(int[][] us, int[][] is, double[][] vs, int size,
                            KeyIndex uidx, KeyIndex iidx) {
        users = us;
        items = is;
        values = vs;
        nprefs = size;
        userIndex = uidx;
        itemIndex = iidx;
    }

    /**
     * Convert an index to a chunk index.
     *
     * @param idx The global index.
     * @return A chunk index.
     */
    static int chunk(int idx) {
        return idx >> CHUNK_SHIFT;
    }

    /**
     * Convert an index to a chunk element index.
     *
     * @param idx The global index.
     * @return The element within the chunk that this refers to
     */
    static int element(int idx) {
        return idx & CHUNK_MASK;
    }

    /**
     * Get the size of this data pack.
     *
     * @return The number of preferences in the data pack.
     */
    public int size() {
        return nprefs;
    }

    /**
     * Get an indirect preference pointed at the specified index.
     *
     * @param index An index in the data pack.
     * @return A preference pointing at the index. This does no checking to make sure that
     *         the preference is valid.
     */
    public IndirectEntry getEntry(int index) {
        return new IndirectEntry(index);
    }

    /**
     * Get the user index mapping between user IDs and indexes.
     *
     * @return The user index.
     */
    public KeyIndex getUserIndex() {
        return userIndex;
    }

    /**
     * Get the item index mapping between user IDs and indexes.
     *
     * @return The item index.
     */
    public KeyIndex getItemIndex() {
        return itemIndex;
    }

    final class IndirectEntry extends RatingMatrixEntry {
        private int index;

        IndirectEntry(int idx) {
            index = idx;
        }

        /**
         * Query whether this getEntry is valid. Valid preferences point to
         *
         * @return {@code true} if this index is valid.
         */
        public boolean isValid() {
            return index >= 0 && index < size();
        }

        private void requireValid() {
            if (!isValid()) {
                throw new IllegalStateException("indirect preference not at valid index");
            }
        }

        @Override
        public long getUserId() {
            return userIndex.getKey(getUserIndex());
        }

        @Override
        public long getItemId() {
            return itemIndex.getKey(getItemIndex());
        }

        @Override
        public double getValue() {
            return values[chunk(index)][element(index)];
        }

        @Override
        public int getIndex() {
            return index;
        }

        public void setIndex(int idx) {
            index = idx;
        }

        @Override
        public int getUserIndex() {
            return users[chunk(index)][element(index)];
        }

        @Override
        public int getItemIndex() {
            return items[chunk(index)][element(index)];
        }
    }
}
