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

/**
 * An entry in a {@link RatingMatrix}.  This is like a {@link Rating}, but:
 *
 * -   It has 0-based indexes for the user, item, and itself.
 * -   It does not have a timestamp, and is not an event.
 */
public abstract class RatingMatrixEntry implements Preference {
    /**
     * Get the user ID for this rating matrix entry.
     * @return The entry's user ID.
     */
    @Override
    public abstract long getUserId();

    /**
     * Get the 0-based user index for this rating matrix entry.
     * @return The entry's user index.  This will correspond to the user's ID in the {@link RatingMatrix#userIndex()} of
     * the rating matrix from which this entry came.
     */
    public abstract int getUserIndex();

    /**
     * Get the item ID for this rating matrix entry.
     * @return The entry's item ID.
     */
    @Override
    public abstract long getItemId();

    /**
     * Get the 0-based item index for this rating matrix entry.
     * @return The entry's item index.  This will correspond to the item's ID in the {@link RatingMatrix#itemIndex()} of
     * the rating matrix from which this entry came.
     */
    public abstract int getItemIndex();

    /**
     * Get the index of this rating in the list of ratings in the matrix.
     * @return The 0-based index of the rating.
     */
    public abstract int getIndex();

    /**
     * Get the value of this rating matrix entry.
     * @return The rating value.
     */
    @Override
    public abstract double getValue();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(RatingMatrixEntry.class)
                          .add("index", getIndex())
                          .add("user", getUserId())
                          .add("item", getItemId())
                          .add("value", getValue())
                          .toString();
    }
}
