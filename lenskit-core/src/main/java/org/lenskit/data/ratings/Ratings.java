/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import com.google.common.base.Function;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.longs.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.data.events.Event;
import org.lenskit.data.events.Events;
import org.lenskit.data.history.UserHistory;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * Utilities for working with ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Ratings {
    private Ratings() {
    }

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
    public static Long2DoubleMap itemRatingVector(@Nonnull Collection<? extends Rating> ratings) {
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
    public static Long2DoubleMap userRatingVector(@Nonnull Collection<Rating> ratings) {
        if (ratings instanceof UserHistory) {
            return ((UserHistory<Rating>) ratings).memoize(RatingVectorFunction.INSTANCE);
        } else {
            return extractVector(ratings, IdExtractor.ITEM);
        }
    }

    /**
     * Obtain a function that converts a user history to a rating vector.
     * @return A function converting a user history to a rating vector.
     */
    public static Function<UserHistory<? extends Event>, Long2DoubleMap> userRatingVectorFunction() {
        return RatingVectorFunction.INSTANCE;
    }

    private static Long2DoubleMap extractVector(Collection<? extends Rating> ratings, IdExtractor dimension) {
        // collect the list of unique IDs
        // use a list since we'll be sorting anyway
        Rating[] rs = ratings.toArray(new Rating[ratings.size()]);
        Arrays.sort(rs, dimension.getComparator());

        LongList ids = new LongArrayList(ratings.size());
        DoubleArrayList values = new DoubleArrayList(ratings.size());

        for (int i = 0; i < rs.length; i++) {
            assert ids.size() == values.size();
            long id = dimension.getId(rs[i]);
            // advance to the last one
            while (i < rs.length - 1 && dimension.getId(rs[i+1]) == id) {
                i++;
            }
            Rating r = rs[i];
            if (r.hasValue()) {
                ids.add(id);
                values.add(r.getValue());
            }
        }

        SortedKeyIndex idx = SortedKeyIndex.fromCollection(ids);
        assert idx.getKeyList().equals(ids);
        return Long2DoubleSortedArrayMap.wrap(idx, values.elements());
    }

    /**
     * Extract a user rating vector from a rating stream.
     *
     * @param ratings The rating stream.
     * @return The user rating vector.
     * @see #userRatingVector(Collection)
     */
    public static Long2DoubleMap userRatingVector(@WillClose ObjectStream<? extends Rating> ratings) {
        return userRatingVector(ObjectStreams.makeList(ratings));
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

            @Override
            Comparator<Event> getComparator() {
                return Events.ITEM_TIME_COMPARATOR;
            }
        },
        USER {
            @Override
            long getId(Event evt) {
                return evt.getUserId();
            }

            @Override
            Comparator<Event> getComparator() {
                return Events.USER_TIME_COMPARATOR;
            }
        };
        abstract long getId(Event evt);
        abstract Comparator<Event> getComparator();
    }

    private enum RatingVectorFunction implements Function<UserHistory<? extends Event>, Long2DoubleMap> {
        INSTANCE;

        @Nullable
        @Override
        public Long2DoubleMap apply(@Nullable UserHistory<? extends Event> input) {
            if (input == null) {
                return null;
            } else {
                // cast to Collection to force non-recursion
                return extractVector(input.filter(Rating.class), IdExtractor.ITEM);
            }
        }
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
