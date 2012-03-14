/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.event;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.WillClose;

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.history.ItemVector;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import com.google.common.primitives.Longs;

/**
 * Utilities for working with ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class Ratings {

    public static final Comparator<Rating> TIMESTAMP_COMPARATOR = new Comparator<Rating>() {
        @Override
        public int compare(Rating r1, Rating r2) {
            return Longs.compare(r1.getTimestamp(), r2.getTimestamp());
        }
    };
    public static final Comparator<Rating> USER_COMPARATOR = new Comparator<Rating>() {
        @Override
        public int compare(Rating r1, Rating r2) {
            return Longs.compare(r1.getUserId(), r2.getUserId());
        }
    };
    public static final Comparator<Rating> ITEM_COMPARATOR = new Comparator<Rating>() {
        @Override
        public int compare(Rating r1, Rating r2) {
            return Longs.compare(r1.getItemId(), r2.getItemId());
        }
    };
    public static final Comparator<Rating> ITEM_TIME_COMPARATOR = new Comparator<Rating>() {
        @Override
        public int compare(Rating r1, Rating r2) {
            long i1 = r1.getItemId();
            long i2 = r2.getItemId();
            if (i1 < i2)
                return -1;
            else if (i1 > i2)
                return 1;
            else
                return Longs.compare(r1.getTimestamp(), r2.getTimestamp());
        }
    };

    /**
     * Construct a rating vector that contains the ratings provided by each user.
     * If all ratings in <var>ratings</var> are for the same item, then this
     * will be a valid item rating vector.  If multiple ratings are by the same
     * user, the one with the highest timestamp is retained.  If two ratings
     * by the same user have identical timestamps, then the one that occurs last
     * when the collection is iterated is retained.
     *
     * @param ratings Some ratings (they should all be for the same item)
     * @return A sparse vector mapping user IDs to ratings.
     * @deprecated Use {@link ItemVector#ratingVector(long, Collection)}.
     */
    @Deprecated
    public static MutableSparseVector itemRatingVector(Collection<? extends Rating> ratings) {
        Long2DoubleMap vect = new Long2DoubleOpenHashMap(ratings.size());
        Long2LongMap tsMap = new Long2LongOpenHashMap(ratings.size());
        tsMap.defaultReturnValue(Long.MIN_VALUE);
        for (Rating r: ratings) {
            long uid = r.getUserId();
            long ts = r.getTimestamp();
            if (ts >= tsMap.get(uid)) {
                vect.put(uid, r.getRating());
                tsMap.put(uid, ts);
            }
        }
        return new MutableSparseVector(vect);
    }

    /**
     * Real implementation of {@link #userRatingVector(Collection)}, using a list
     * we are free to sort.
     * @param ratings
     * @return A vector containing the ratings of the list.
     */
    @Deprecated
    private static MutableSparseVector userRatingVector(ArrayList<Rating> ratings) {
        Rating rp = null;
        for (Rating r: ratings) {
            if (rp != null && ITEM_TIME_COMPARATOR.compare(rp, r) > 0) {
                Collections.sort(ratings, ITEM_TIME_COMPARATOR);
                break;
            }
            rp = r;
        }

        // collect the list of unique item IDs
        long[] items = new long[ratings.size()];
        double[] values = new double[ratings.size()];
        int li = -1;
        for (Rating r: ratings) {
            long iid = r.getItemId();
            if (li < 0 || items[li] != iid)
                li++;
            items[li] = iid;
            values[li] = r.getRating();
        }

        return MutableSparseVector.wrap(items, values, li+1);
    }

    /**
     * Construct a rating vector that contains the ratings provided for each
     * item. If all ratings in <var>ratings</var> are by the same user, then
     * this will be a valid user rating vector. If multiple ratings are provided
     * for the same item, the one with the greatest timestamp is retained. Ties
     * are broken by preferring ratings which come later when iterating through
     * the collection.
     *
     * @param ratings A collection of ratings (should all be by the same user)
     * @return A sparse vector mapping item IDs to ratings
     * @deprecated Use {@link UserVector#fromRatings(long, Collection)}
     *             instead.
     */
    @Deprecated
    public static MutableSparseVector userRatingVector(Collection<? extends Rating> ratings) {
        return userRatingVector(new ArrayList<Rating>(ratings));
    }

    /**
     * Extract a user rating vector from a rating cursor.
     *
     * @param ratings The rating cursor.
     * @return The user rating vector.
     * @see #userRatingVector(Collection)
     * @deprecated Use {@link UserVector#fromRatings(long, Cursor)}
     *             instead.
     */
    @Deprecated
    public static MutableSparseVector userRatingVector(@WillClose Cursor<? extends Rating> ratings) {
        return userRatingVector(Cursors.makeList(ratings));
    }

    /**
     * Make a fresh rating object with no timestamp.
     * @see #make(long, long, double, long)
     */
    public static Rating make(long uid, long iid, double value) {
        return make(uid, iid, value, -1);
    }

    /**
     * Make a fresh rating event. Event IDs are generated sequentially. This is
     * mostly useful in test cases.
     */
    public static Rating make(long uid, long iid, double value, long ts) {
        return new SimpleRating(Events.nextEventId.incrementAndGet(),
                                uid, iid, value, ts);
    }
}
