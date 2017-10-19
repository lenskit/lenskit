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

import com.google.common.base.Equivalence;
import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Utilities for working with ratings.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Ratings {
    private Ratings() {
    }

    /**
     * Compare two events by timestamp.
     */
    public static final Comparator<Rating> TIMESTAMP_COMPARATOR = new Comparator<Rating>() {
        @Override
        public int compare(Rating e1, Rating e2) {
            return Longs.compare(e1.getTimestamp(), e2.getTimestamp());
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
     */
    public static Long2DoubleMap itemRatingVector(@Nonnull Collection<? extends Rating> ratings) {
        return extractVector(ratings.iterator(), IdExtractor.USER, ratings.size());
    }

    /**
     * Construct a rating vector that contains the ratings provided for each
     * item. If all ratings in <var>ratings</var> are by the same user, then
     * this will be a valid user rating vector.
     *
     * @param ratings A collection of ratings (should all be by the same user)
     * @return A sparse vector mapping item IDs to ratings
     */
    public static Long2DoubleMap userRatingVector(@Nonnull Collection<Rating> ratings) {
        return extractVector(ratings.iterator(), IdExtractor.ITEM, ratings.size());
    }

    private static Long2DoubleMap extractVector(Iterator<? extends Rating> ratings, IdExtractor dimension, int n) {
        LongArrayList ids = new LongArrayList(n > 0 ? n : LongArrayList.DEFAULT_INITIAL_CAPACITY);
        DoubleArrayList values = new DoubleArrayList(n > 0 ? n : DoubleArrayList.DEFAULT_INITIAL_CAPACITY);

        while (ratings.hasNext()) {
            Rating r = ratings.next();
            assert ids.size() == values.size();
            long id = dimension.getId(r);
            ids.add(id);
            values.add(r.getValue());
        }

        ids.trim();
        values.trim();
        assert ids.elements().length == ids.size();
        assert values.elements().length == values.size();
        return Long2DoubleSortedArrayMap.wrapUnsorted(ids.elements(), values.elements());
    }

    /**
     * Extract a user rating vector from a rating stream.
     *
     * @param ratings The rating stream.
     * @return The user rating vector.
     * @see #userRatingVector(Collection)
     */
    public static Long2DoubleMap userRatingVector(@WillClose ObjectStream<? extends Rating> ratings) {
        return extractVector(ratings.iterator(), IdExtractor.ITEM, -1);
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

    private enum IdExtractor implements Comparator<Rating> {
        ITEM {
            @Override
            long getId(Rating evt) {
                return evt.getItemId();
            }

            @Override
            public int compare(Rating o1, Rating o2) {
                return Longs.compare(o1.getItemId(), o2.getItemId());
            }
        },
        USER {
            @Override
            long getId(Rating evt) {
                return evt.getUserId();
            }

            @Override
            public int compare(Rating o1, Rating o2) {
                return Longs.compare(o1.getUserId(), o2.getUserId());
            }
        };
        abstract long getId(Rating evt);
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
