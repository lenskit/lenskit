/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.event;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utilities for working with ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Ratings {
    /**
     * Integer to generate sequential IDs for fresh events.  Used mostly in
     * test cases.
     */
    static final AtomicLong nextEventId = new AtomicLong();

    public static final Comparator<Rating> ITEM_TIME_COMPARATOR = new Comparator<Rating>() {
        @Override
        public int compare(Rating r1, Rating r2) {
            long i1 = r1.getItemId();
            long i2 = r2.getItemId();
            if (i1 < i2) {
                return -1;
            } else if (i1 > i2) {
                return 1;
            } else {
                return Longs.compare(r1.getTimestamp(), r2.getTimestamp());
            }
        }
    };

    /**
     * Construct a rating vector that contains the ratings provided by each user.
     * If all ratings in {@var ratings} are for the same item, then this
     * will be a valid item rating vector.  If multiple ratings are by the same
     * user, the one with the highest timestamp is retained.  If two ratings
     * by the same user have identical timestamps, then the one that occurs last
     * when the collection is iterated is retained.
     *
     * @param ratings Some ratings (they should all be for the same item)
     * @return A sparse vector mapping user IDs to ratings.
     */
    public static MutableSparseVector itemRatingVector(Collection<? extends Rating> ratings) {
        Long2DoubleMap v = new Long2DoubleOpenHashMap(ratings.size());
        Long2LongMap tsMap = new Long2LongOpenHashMap(ratings.size());
        tsMap.defaultReturnValue(Long.MIN_VALUE);
        for (Rating r : ratings) {
            long uid = r.getUserId();
            long ts = r.getTimestamp();
            if (ts >= tsMap.get(uid)) {
                Preference p = r.getPreference();
                if (p != null) {
                    v.put(uid, p.getValue());
                } else {
                    v.remove(uid);
                }
                tsMap.put(uid, ts);
            }
        }
        return new MutableSparseVector(v);
    }

    /**
     * Construct a rating vector that contains the ratings provided for each
     * item. If all ratings in {@var ratings} are by the same user, then
     * this will be a valid user rating vector. If multiple ratings are provided
     * for the same item, the one with the greatest timestamp is retained. Ties
     * are broken by preferring ratings which come later when iterating through
     * the collection.
     *
     * @param ratings A collection of ratings (should all be by the same user)
     * @return A sparse vector mapping item IDs to ratings
     */
    public static MutableSparseVector userRatingVector(Collection<? extends Rating> ratings) {
        // sort ratings by item, then time, so we can overwrite previous ratings
        // since Java 1.7, this uses TimSort, so it is fast on pre-sorted lists
        Rating[] sortedRatings = ratings.toArray(new Rating[ratings.size()]);
        Arrays.sort(sortedRatings, ITEM_TIME_COMPARATOR);

        // collect the list of unique item IDs
        long[] items = new long[sortedRatings.length];
        double[] values = new double[sortedRatings.length];
        int li = -1;
        for (Rating r : sortedRatings) {
            long iid = r.getItemId();
            // is this an unseen item?
            if (li < 0 || items[li] != iid) {
                li++;
                items[li] = iid;
            }

            Preference p = r.getPreference();
            if (p != null) {
                // save the preference
                values[li] = p.getValue();
            } else {
                // pretend we haven't seen the last item
                li--;
            }
        }

        return MutableSparseVector.wrap(items, values, li + 1);
    }

    /**
     * Extract a user rating vector from a rating cursor.
     *
     * @param ratings The rating cursor.
     * @return The user rating vector.
     * @see #userRatingVector(Collection)
     */
    public static MutableSparseVector userRatingVector(@WillClose Cursor<? extends Rating> ratings) {
        return userRatingVector(Cursors.makeList(ratings));
    }

    /**
     * Make a fresh rating object with no timestamp.
     *
     * @see #make(long, long, double, long)
     */
    public static Rating make(long uid, long iid, double value) {
        return new RatingBuilder().setUserId(uid)
                                  .setItemId(iid)
                                  .setRating(value)
                                  .build();
    }

    /**
     * Make a fresh rating event. Event IDs are generated sequentially. This is
     * mostly useful in test cases.
     */
    public static Rating make(long uid, long iid, double value, long ts) {
        return new RatingBuilder().setUserId(uid)
                                  .setItemId(iid)
                                  .setRating(value)
                                  .setTimestamp(ts)
                                  .build();
    }

    /**
     * Construct a new {@link RatingBuilder}.
     * @return A new rating builder.
     * @since 1.3
     */
    public static RatingBuilder newBuilder() {
        return new RatingBuilder();
    }

    /**
     * Construct a rating builder initialized with the values of a rating.
     * @param r The rating.
     * @return A rating builder that will initially build a copy of {@var r}.
     * @since 1.e
     */
    public static RatingBuilder copyBuilder(@Nonnull Rating r) {
        Preconditions.checkNotNull(r, "rating");
        RatingBuilder rb = newBuilder();
        rb.setUserId(r.getUserId())
          .setItemId(r.getItemId())
          .setTimestamp(r.getTimestamp());
        Preference pref = r.getPreference();
        if (pref == null) {
            rb.clearRating();
        } else {
            rb.setRating(pref.getValue());
        }
        return rb;
    }
}
