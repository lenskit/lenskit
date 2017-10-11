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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.lenskit.data.entities.*;

import java.io.Serializable;

/**
 * A user rating an item. A rating is an expression of preference, in the form of a real-valued rating, for an item by
 * a user.  Ratings are also used to represent un-rate events, if the system supports them; these are represented by
 * a rating value of {@link Double#NaN}.
 *
 * To create a rating, use {@link RatingBuilder}.  The {@link #newBuilder()} method will create a rating builder.
 *
 * @compat Public
 */
@BuiltBy(RatingBuilder.class)
@DefaultEntityType("rating")
public class Rating extends AbstractBeanEntity implements Preference, Serializable {
    private static final long serialVersionUID = 2L;
    private static final EntityFactory factory = new EntityFactory();
    public static final EntityType ENTITY_TYPE = CommonTypes.RATING;
    public static final AttributeSet ATTRIBUTES = AttributeSet.create(CommonAttributes.ENTITY_ID,
                                                                      CommonAttributes.USER_ID,
                                                                      CommonAttributes.ITEM_ID,
                                                                      CommonAttributes.RATING,
                                                                      CommonAttributes.TIMESTAMP);
    private static final BeanEntityLayout LAYOUT = makeLayout(Rating.class);

    private final long user;
    private final long item;
    private final double value;

    Rating(long eid, long uid, long iid, double v) {
        super(LAYOUT, CommonTypes.RATING, eid);
        user = uid;
        item = iid;
        value = v;
    }

    private Rating(BeanEntityLayout layout, long eid, long uid, long iid, double v) {
        super(layout, CommonTypes.RATING, eid);
        user = uid;
        item = iid;
        value = v;
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
        return factory.rating(uid, iid, rating, ts);
    }

    /**
     * Create a new rating builder.
     * @return A new rating builder.
     */
    public static RatingBuilder newBuilder() {
        return new RatingBuilder();
    }

    @Override
    @EntityAttribute("user")
    public long getUserId() {
        return user;
    }

    @Override
    @EntityAttribute("item")
    public long getItemId() {
        return item;
    }

    public long getTimestamp() {
        return -1;
    }

    /**
     * Get the rating value.
     *
     * @return double The rating value, or {@link Double#NaN} if the rating has no value.
     */
    @EntityAttribute("rating")
    public double getValue() {
        return value;
    }

    /**
     * Create a new rating builder that will build a copy of this rating.
     * @return A rating builder initialized with the contents of this rating.
     */
    public RatingBuilder copyBuilder() {
        RatingBuilder rb = new RatingBuilder();
        rb.setUserId(user)
          .setItemId(item)
          .setTimestamp(getTimestamp());
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
                                      .append(getTimestamp(), r.getTimestamp())
                                      .isEquals();
        } else {
            return super.equals(obj);
        }
    }

    public static class WithTimestamp extends Rating {
        private static final BeanEntityLayout TIMESTAMP_LAYOUT = makeLayout(WithTimestamp.class);
        private final long timestamp;

        WithTimestamp(long id, long user, long item, double val, long ts) {
            super(TIMESTAMP_LAYOUT, id, user, item, val);
            timestamp = ts;
        }

        @Override
        @EntityAttribute("timestamp")
        public long getTimestamp() {
            return timestamp;
        }
    }
}
