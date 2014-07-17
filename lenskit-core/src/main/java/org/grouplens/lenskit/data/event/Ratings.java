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
package org.grouplens.lenskit.data.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.util.Collection;
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

    public static final Ordering<Rating> ITEM_TIME_COMPARATOR = new Ordering<Rating>() {
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
    public static MutableSparseVector itemRatingVector(@Nonnull Collection<? extends Rating> ratings) {
        return extractVector(ratings, IdExtractor.USER);
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
    public static MutableSparseVector userRatingVector(@Nonnull Collection<? extends Rating> ratings) {
        return extractVector(ratings, IdExtractor.ITEM);

    }

    private static MutableSparseVector extractVector(Collection<? extends Rating> ratings, IdExtractor dimension) {
        // collect the list of unique IDs
        // use a list since we'll be sorting anyway
        LongList ids = new LongArrayList(ratings.size());
        for (Rating r: CollectionUtils.fast(ratings)) {
            ids.add(dimension.getId(r));
        }

        LongKeyDomain keys = LongKeyDomain.fromCollection(ids, false);
        MutableSparseVector msv = MutableSparseVector.create(keys.domain());
        long[] timestamps = null;
        // check for fast-path, where each item has one rating
        if (keys.domainSize() < ratings.size()) {
            timestamps = new long[keys.domainSize()];
        }

        for (Rating r: CollectionUtils.fast(ratings)) {
            long id = dimension.getId(r);
            if (timestamps != null) {
                int idx = keys.getIndex(id);
                if (keys.indexIsActive(idx) && timestamps[idx] >= r.getTimestamp()) {
                    continue;  // we have seen a newer event - skip this.
                } else {
                    timestamps[idx] = r.getTimestamp();
                    keys.setActive(idx, true);
                }
            }

            Preference p = r.getPreference();
            if (p != null) {
                // save the preference
                msv.set(id, p.getValue());
            } else {
                msv.unset(id);
            }
        }

        return msv;
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

    private static enum IdExtractor {
        ITEM {
            @Override
            long getId(Event evt) {
                return evt.getItemId();
            }
        },
        USER {
            @Override
            long getId(Event evt) {
                return evt.getUserId();
            }
        };
        abstract long getId(Event evt);
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

    /**
     * Compute the hash code of a rating.  Used to implement {@link #hashCode()} in rating
     * implementations.
     * @param rating The rating.
     * @return The rating's hash code.
     */
    public static int hashRating(Rating rating) {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(rating.getUserId())
           .append(rating.getItemId());
        if (rating.hasValue()) {
            hcb.append(rating.getValue());
        }
        return hcb.toHashCode();
    }

    /**
     * Compare two ratings for equality.  Used to implement {@link #equals(Object)} in rating
     * implementations.
     * @param r1 The first rating.
     * @param r2 The second rating.
     * @return Whether the two ratings are equal.
     */
    public static boolean equals(Rating r1, Rating r2) {
        if (r1 == r2) {
            return true;
        } else if (r1 == null || r2 == null) {
            return false;
        }

        Preference p1 = r1.getPreference();
        Preference p2 = r2.getPreference();
        if (p1 != null && p2 != null) {
            return r1.getUserId() == r2.getUserId()
                    && r1.getItemId() == r2.getItemId()
                    && r1.getTimestamp() == r2.getTimestamp();
        } else if (p1 != null) {
            return p1.equals(p2);
        } else {
            // p1 is null, check p2
            return p2 == null;
        }
    }
}
