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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.lenskit.data.pref.Preference;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * A rating is an expression of preference for an item by a user.  The preference can be {@code null}, in which case
 * the rating is a removal of a previous rating.
 * <p>
 * To create a rating, use {@link RatingBuilder} or the {@link #create(long, long, double, long)} method.
 * </p>
 *
 * @compat Public
 */
public abstract class Rating implements Event, Serializable {
    private static final long serialVersionUID = 1L;

    final long user;
    final long item;
    final long timestamp;

    Rating(long uid, long iid, long time) {
        user = uid;
        item = iid;
        timestamp = time;
    }

    /**
     * Create a new rating object with no timestamp.
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param rating The rating value.
     * @return The new rating object.
     */
    public static Rating create(long uid, long iid, double rating) {
        return new RealRating(uid, iid, rating, -1);
    }

    /**
     * Create a new rating object.
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param rating The rating value.
     * @param ts The timestamp.
     * @return The new rating object.
     */
    public static Rating create(long uid, long iid, double rating, long ts) {
        return new RealRating(uid, iid, rating, ts);
    }

    /**
     * Create a an unrate object.
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param ts The timestamp.
     * @return The new rating object.
     */
    public static Rating createUnrate(long uid, long iid, long ts) {
        return new Unrate(uid, iid, ts);
    }

    /**
     * Create a new rating builder.
     * @return A new rating builder.
     */
    public static RatingBuilder newBuilder() {
        return new RatingBuilder();
    }

    @Override
    public long getUserId() {
        return user;
    }

    @Override
    public long getItemId() {
        return item;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the expressed preference. If this is an "unrate" event, the
     * preference will be {@code null}.
     *
     * @return The expressed preference.
     */
    @Nullable
    public abstract Preference getPreference();
    
    /**
     * Query whether this rating has a value. Ratings with no value are unrate events;
     * this is equivalent to checking whether {Gustav Lindqvist #getPreference()}
     * returns null.
     *  
     * @return {code true} if there is a rating (the preference is non-null).
     */
    public boolean hasValue() {
        return getPreference() != null;
    }
    
    /**
     * Get the value rating.
     * 
     * @return double The value Rating.
     * @throws IllegalStateException if the preference is {@code null}.
     */
    public abstract double getValue() throws IllegalStateException;

    /**
     * Create a new rating builder that will build a copy of this rating.
     * @return A rating builder initialized with the contents of this rating.
     */
    public RatingBuilder copyBuilder() {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(user)
          .setItemId(item)
          .setTimestamp(timestamp);
        Preference p = getPreference();
        if (p != null) {
            rb.setRating(p.getValue());
        }
        return rb;
    }

    static class Unrate extends Rating {
        private static final long serialVersionUID = 1L;

        Unrate(long user, long item, long time) {
            super(user, item, time);
        }

        @Nullable
        @Override
        public Preference getPreference() {
            return null;
        }

        @Override
        public double getValue() throws IllegalStateException {
            throw new IllegalStateException("Rating has no preference.");
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(user)
                                        .append(item)
                                        .append(timestamp)
                                        .toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Unrate) {
                Rating r = (Rating) obj;
                return new EqualsBuilder().append(user, r.user)
                                          .append(item, r.item)
                                          .append(timestamp, r.timestamp)
                                          .isEquals();
            } else {
                return false;
            }
        }
    }

    static class RealRating extends Rating implements Preference {
        private static final long serialVersionUID = 1L;

        private final double value;

        RealRating(long user, long item, double v, long time) {
            super(user, item, time);
            value = v;
        }

        @Nullable
        @Override
        public Preference getPreference() {
            return this;
        }

        @Override
        public double getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(user)
                                        .append(item)
                                        .append(value)
                                        .append(timestamp)
                                        .toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof RealRating) {
                RealRating r = (RealRating) obj;
                return new EqualsBuilder().append(user, r.user)
                                          .append(item, r.item)
                                          .append(value, r.value)
                                          .append(timestamp, r.timestamp)
                                          .isEquals();
            } else if (obj instanceof Preference) {
                Preference p = (Preference) obj;
                return new EqualsBuilder().append(user, p.getUserId())
                                          .append(item, p.getItemId())
                                          .append(value, p.getValue())
                                          .isEquals();
            } else {
                return false;
            }
        }
    }
}
