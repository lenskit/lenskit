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
package org.lenskit.util.keys;

import it.unimi.dsi.fastutil.longs.LongList;

/**
 * Bidirectional mapping between long keys and a contiguous range of integer indexes.
 */
public interface KeyIndex {
    /**
     * Get the index of a key.
     *
     * @param key The key to query.
     * @return The key's index.
     * @throws IllegalArgumentException if {@code key} is not in the index.
     */
    int getIndex(long key);

    /**
     * Query whether this index contains a particular key.
     * @param key The key to look for.
     * @return {@code true} if the index contains the key.
     */
    boolean containsKey(long key);

    /**
     * Get the key for an index position.
     *
     * @param idx The index of the key to retrieve.
     * @return The key for the given <var>idx</var>.
     * @throws IndexOutOfBoundsException if {@code idx} is not a valid index.
     */
    long getKey(int idx);

    /**
     * Try to get the index for an ID, returning a negative value if it does not exist.
     * This method is like {@link #getIndex(long)}, except it returns a negative value
     * instead of throwing an exception if the id does not exist.
     * @param id The ID to look for.
     * @return The index of the ID, or a negative value if it is not in the index.
     */
    int tryGetIndex(long id);

    /**
     * Get the size of this index.
     *
     * @return The number of indexed keys.
     */
    int size();

    /**
     * Get the lower bound of the index range for this index.
     * @return The lower bound for the key index range.
     */
    int getLowerBound();

    /**
     * Get the upper bound of the index range for this index.
     *
     * @return The upper bound for the key index range.
     */
    int getUpperBound();

    /**
     * Get the list of indexed keys.  This list is 0-indexed, so the key at position 0 in this list is at index
     * {@link #getLowerBound()} in the key index.
     *
     * @return The list of keys in the index.  No key will appear twice.
     */
    LongList getKeyList();

    /**
     * Get a frozen copy of this key index.  If the key index is mutable, then this method will return an immutable
     * copy of it.  If the key index is already immutable, it may just return itself without copying.
     *
     * @return An immutable key index with the same contents as this key index.
     */
    KeyIndex frozenCopy();
}
