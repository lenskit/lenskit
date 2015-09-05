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

import com.google.common.base.Equivalence;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.collections.LongKeyDomain;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.data.events.Event;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.util.Collection;

/**
 * Utilities for working with ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Ratings {
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
     */
    public static MutableSparseVector itemRatingVector(@Nonnull Collection<? extends Rating> ratings) {
        return extractVector(ratings, IdExtractor.USER);
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
     */
    public static MutableSparseVector userRatingVector(@Nonnull Collection<? extends Rating> ratings) {
        return extractVector(ratings, IdExtractor.ITEM);
    }

    private static MutableSparseVector extractVector(Collection<? extends Rating> ratings, IdExtractor dimension) {
        // collect the list of unique IDs
        // use a list since we'll be sorting anyway
        LongList ids = new LongArrayList(ratings.size());
        for (Rating r: ratings) {
            ids.add(dimension.getId(r));
        }

        LongKeyDomain keys = LongKeyDomain.fromCollection(ids, false);
        MutableSparseVector msv = MutableSparseVector.create(keys.domain());
        long[] timestamps = null;
        // check for fast-path, where each item has one rating
        if (keys.domainSize() < ratings.size()) {
            timestamps = new long[keys.domainSize()];
        }

        for (Rating r: ratings) {
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

            if (r.hasValue()) {
                // save the getEntry
                msv.set(id, r.getValue());
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

    /**
     * An equivalence relation over preferences.
     * @return An equivalence relation over preferences where two preferences are equivalent if they have
     * the same user, item, and value.
     */
    public static Equivalence<Preference> preferenceEquivalence() {
        return PrefEquiv.INSTANCE;
    }

    private static class PrefEquiv extends Equivalence<Preference> {
        private static final PrefEquiv INSTANCE = new PrefEquiv();

        @Override
        protected boolean doEquivalent(@Nonnull Preference a, @Nonnull Preference b) {
            EqualsBuilder eqb = new EqualsBuilder();
            return eqb.append(a.getUserId(), b.getUserId())
                      .append(a.getItemId(), b.getItemId())
                      .append(a.getValue(), b.getValue())
                      .isEquals();
        }

        @Override
        protected int doHash(@Nonnull Preference p) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            return hcb.append(p.getUserId())
                      .append(p.getItemId())
                      .append(p.getValue())
                      .toHashCode();
        }
    }

    private enum IdExtractor {
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
     * @deprecated Use {@link Rating#create(long, long, double)}.
     */
    @Deprecated
    public static Rating make(long uid, long iid, double value) {
        return new RatingBuilder().setUserId(uid)
                                  .setItemId(iid)
                                  .setRating(value)
                                  .build();
    }

    /**
     * Make a fresh rating event.
     *
     * @deprecated Use {@link Rating#create(long, long, double, long)}
     */
    @Deprecated
    public static Rating make(long uid, long iid, double value, long ts) {
        return new RatingBuilder().setUserId(uid)
                                  .setItemId(iid)
                                  .setRating(value)
                                  .setTimestamp(ts)
                                  .build();
    }
}
