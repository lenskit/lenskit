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
