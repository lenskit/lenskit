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
package org.lenskit.data.ratings;

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
