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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.keys.KeyIndex;

import net.jcip.annotations.Immutable;
import java.util.Collection;
import java.util.List;

/**
 * Snapshot of the ratings data for building a recommender.
 *
 * This provides a snapshot of each user's most current rating for each item.  It may also represent synthetic ratings
 * derived from other types of events, depending on the builder that is used for it.
 *
 * The ratings obtained from a rating matrix **do not** have timestamps.
 *
 * The users, items, and ratings in the rating matrix are associated with 0-based indexes, so that they can be used
 * in conjunction with vectors or arrays.  The rating matrix can be thought of as a sparse matrix in coordinate list
 * (COO) format, and the index of the rating is its position in the coordinate list.
 */
@Immutable
@DefaultImplementation(PackedRatingMatrix.class)
public interface RatingMatrix {
    /**
     * Get the set of user IDs in the snapshot.
     *
     * @return A set of all known user IDs.
     */
    LongCollection getUserIds();

    /**
     * Get the set of item IDs in the snapshot.
     *
     * @return A set of all known item IDs.
     */
    LongCollection getItemIds();

    /**
     * Get the user ID index.
     *
     * @return The index mapping between user IDs and user indices.
     */
    KeyIndex userIndex();

    /**
     * Get the item ID index.
     *
     * @return The index mapping between user IDs and user indices.
     */
    KeyIndex itemIndex();

    /**
     * Get the collection of ratings in the snapshot. The ratings are returned in an undetermined
     * order. It is guaranteed that no duplicate ratings appear - each <i>(user,item)</i> pair is
     * rated at most once. Each preference's index is also in the range [0,len), where len is the
     * size of this collection.
     *
     * <p> Modifying the returned indexed preferences will <b>not</b> modify the underlying
     * snapshot.
     *
     * @return All ratings in the system.
     */
    List<RatingMatrixEntry> getRatings();

    /**
     * Get the ratings for a particular user. It is guaranteed that no duplicate ratings appear -
     * each <i>(user,item)</i> pair is rated at most once.
     *
     * <p>Modifying the returned indexed preferences will <b>not</b> modify the underlying
     * snapshot.
     *
     * @param userId The user's ID.
     * @return The user's ratings, or an empty collection if the user is unknown.
     */
    Collection<RatingMatrixEntry> getUserRatings(long userId);

    /**
     * Get the current preferences of a particular user as a vector.
     *
     * @param userId The user's ID.
     * @return The user's rating vector.
     */
    Long2DoubleMap getUserRatingVector(long userId);
}
