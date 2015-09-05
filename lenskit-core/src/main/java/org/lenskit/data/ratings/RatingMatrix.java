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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import org.grouplens.grapht.annotation.DefaultImplementation;
import org.lenskit.util.keys.KeyIndex;

import javax.annotation.concurrent.Immutable;
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
