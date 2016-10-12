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

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.lenskit.data.entities.*;
import org.lenskit.data.events.Event;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Set;

/**
 * A user rating an item. A rating is an expression of preference, in the form of a real-valued rating, for an item by
 * a user.  Ratings are also used to represent un-rate events, if the system supports them; these are represented by
 * a rating value of {@link Double#NaN}.
 * <p>
 * To create a rating, use {@link RatingBuilder} or the factory methods {@link #create(long, long, double, long)},
 * {@link #create(long, long, double)}, and {@link #createUnrate(long, long, long)}.
 * </p>
 *
 * @compat Public
 */
@org.lenskit.data.events.BuiltBy(RatingBuilder.class)
@BuiltBy(RatingBuilder.class)
@DefaultEntityType("rating")
public final class Rating extends AbstractEntity implements Event, Preference, Serializable {
    private static final long serialVersionUID = 2L;
    private static final EntityFactory factory = new EntityFactory();
    private static final Set<TypedName<?>> FULL_ATTR_NAMES =
            ImmutableSet.<TypedName<?>>of(CommonAttributes.ENTITY_ID,
                                          CommonAttributes.USER_ID,
                                          CommonAttributes.ITEM_ID,
                                          CommonAttributes.RATING,
                                          CommonAttributes.TIMESTAMP);
    private static final Set<TypedName<?>> NOTIME_ATTR_NAMES =
            ImmutableSet.<TypedName<?>>of(CommonAttributes.ENTITY_ID,
                                          CommonAttributes.USER_ID,
                                          CommonAttributes.ITEM_ID,
                                          CommonAttributes.RATING);


    private final long user;
    private final long item;
    private final double value;
    private final long timestamp;

    Rating(long eid, long uid, long iid, double v, long time) {
        super(CommonTypes.RATING, eid);
        user = uid;
        item = iid;
        value = v;
        timestamp = time;
    }

    /**
     * Create a new rating object with no timestamp.
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param rating The rating value.  Cannot be NaN.
     * @return The new rating object.
     * @see #create(long, long, double, long)
     * @deprecated Use {@link EntityFactory#rating(long, long, double)}
     */
    @Deprecated
    public static Rating create(long uid, long iid, double rating) {
        return create(uid, iid, rating, -1);
    }

    /**
     * Create a new rating object.
     * <p>
     * In order to prevent computation errors from producing unintended unrate events, this method cannot be used to
     * create an unrate event.  Instead, use {@link #createUnrate(long, long, long)}.
     * </p>
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param rating The rating value. Cannot be NaN.
     * @param ts The timestamp.
     * @return The new rating object.
     * @throws IllegalArgumentException if {@code rating} is NaN.
     * @deprecated Use {@link EntityFactory#rating(long, long, double, long)}
     */
    @Deprecated
    public static Rating create(long uid, long iid, double rating, long ts) {
        if (Double.isNaN(rating)) {
            throw new IllegalArgumentException("rating is not a number");
        }
        return factory.rating(uid, iid, rating, ts);
    }

    /**
     * Create a an unrate object.
     *
     * @param uid The user ID.
     * @param iid The item ID.
     * @param ts The timestamp.
     * @return The new rating object.
     * @deprecated Unrates have gone away.
     */
    @Deprecated
    public static Rating createUnrate(long uid, long iid, long ts) {
        return new Rating(-1, uid, iid, Double.NaN, ts);
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
     * Query whether this rating has a value. Ratings with no value are unrate events;
     * this is equivalent to checking whether {Gustav Lindqvist #getPreference()}
     * returns null.
     *  
     * @return {code true} if there is a rating (the preference is non-null).
     * @deprecated Unrates have gone away.
     */
    @Deprecated
    public boolean hasValue() {
        return !Double.isNaN(value);
    }
    
    /**
     * Get the rating value.
     *
     * @return double The rating value, or {@link Double#NaN} if the rating has no value.
     */
    public double getValue() {
        return value;
    }

    @Override
    public Set<TypedName<?>> getTypedAttributeNames() {
        return timestamp >= 0 ? FULL_ATTR_NAMES : NOTIME_ATTR_NAMES;
    }

    @Override
    public boolean hasAttribute(String name) {
        switch (name) {
        case "id":
        case "user":
        case "item":
        case "rating":
            return true;
        case "timestamp":
            return timestamp > -1;
        default:
            return false;
        }
    }

    @Nullable
    @Override
    public Object maybeGet(String attr) {
        switch (attr) {
        case "id":
            return id;
        case "user":
            return user;
        case "item":
            return item;
        case "rating":
            return value;
        case "timestamp":
            return timestamp >= 0 ? timestamp : null;
        default:
            return null;
        }
    }

    /**
     * Create a new rating builder that will build a copy of this rating.
     * @return A rating builder initialized with the contents of this rating.
     */
    public RatingBuilder copyBuilder() {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(user)
          .setItemId(item)
          .setTimestamp(timestamp);
        double v = getValue();
        if (!Double.isNaN(v)) {
            rb.setRating(v);
        }
        return rb;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Rating) {
            Rating r = (Rating) obj;
            return new EqualsBuilder().append(user, r.user)
                                      .append(item, r.item)
                                      .append(value, r.value)
                                      .append(timestamp, r.timestamp)
                                      .isEquals();
        } else {
            return super.equals(obj);
        }
    }
}
