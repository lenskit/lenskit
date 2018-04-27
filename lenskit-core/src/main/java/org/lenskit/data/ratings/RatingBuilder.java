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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.lenskit.data.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Build a {@link Rating}.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RatingBuilder extends AbstractBeanEntityBuilder implements Builder<Rating> {
    private static final Logger logger = LoggerFactory.getLogger(RatingBuilder.class);
    private static final AtomicLong idGenerator = new AtomicLong();
    private static volatile boolean hasWarned;

    private boolean hasUserId;
    private long userId;
    private boolean hasItemId;
    private long itemId;
    private boolean hasRating;
    private double rating;
    private long timestamp = -1;

    /**
     * Create an uninitialized rating builder.
     */
    public RatingBuilder() {
        super(CommonTypes.RATING);
    }

    /**
     * Create an unitialized rating builder.
     * @param type The rating builder.
     */
    public RatingBuilder(EntityType type) {
        super(type);
        if (type != CommonTypes.RATING) {
            throw new IllegalArgumentException("only 'rating' entities can be viewed as ratings");
        }
    }

    /**
     * Construct a new rating builder that is a copy of a particular rating.
     * @param r The rating to copy.
     * @return A rating builder that will initially build a copy of the specified rating.
     */
    public static RatingBuilder copy(Rating r) {
        return r.copyBuilder();
    }

    @Override
    public RatingBuilder reset() {
        super.reset();
        hasUserId = hasItemId = hasRating = false;
        timestamp = -1;
        return this;
    }

    /**
     * Get the rating ID.
     * @return The rating ID.
     */
    public long getId() {
        return id;
    }

    /**
     * Set the rating ID.
     * @param id The rating ID.
     * @return The builder (for chaining).
     */
    @EntityAttributeSetter("id")
    public RatingBuilder setId(long id) {
        return (RatingBuilder) super.setId(id);
    }

    /**
     * Get the user ID.
     * @return The user ID.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Set the user ID.
     * @param uid The user ID.
     * @return The builder (for chaining).
     */
    @EntityAttributeSetter("user")
    public RatingBuilder setUserId(long uid) {
        userId = uid;
        hasUserId = true;
        return this;
    }

    /**
     * Get the item ID.
     * @return The item ID.
     */
    public long getItemId() {
        return itemId;
    }

    /**
     * Set the item ID.
     * @param iid The item ID.
     * @return The builder (for chaining).
     */
    @EntityAttributeSetter("item")
    public RatingBuilder setItemId(long iid) {
        itemId = iid;
        hasItemId = true;
        return this;
    }

    /**
     * Clear the item ID.
     * @return The builder (for chaining).
     */
    @EntityAttributeClearer("item")
    public RatingBuilder clearItemId() {
        hasItemId = false;
        return this;
    }

    /**
     * Clear the user ID.
     * @return The builder (for chaining).
     */
    @EntityAttributeClearer("user")
    public RatingBuilder clearUserId() {
        hasUserId = false;
        return this;
    }

    /**
     * Get the rating.
     * @return The rating value.
     */
    public double getRating() {
        return rating;
    }

    /**
     * Set the rating value.
     *
     * @param r The rating value.
     * @return The builder (for chaining).
     */
    @EntityAttributeSetter("rating")
    public RatingBuilder setRating(double r) {
        if (Double.isNaN(r)) {
            throw new IllegalArgumentException("rating is not a number");
        }
        rating = r;
        hasRating = true;
        return this;
    }

    /**
     * Clear the rating value.
     * @return The builder (for chaining).
     */
    @EntityAttributeClearer("rating")
    public RatingBuilder clearRating() {
        hasRating = false;
        return this;
    }

    /**
     * Get the timestamp.
     * @return The timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp.
     * @param ts The timestamp.
     * @return The builder (for chaining).
     */
    @EntityAttributeSetter("timestamp")
    public RatingBuilder setTimestamp(long ts) {
        timestamp = ts;
        return this;
    }

    @EntityAttributeClearer("timestamp")
    public RatingBuilder clearTimestamp() {
        return setTimestamp(-1);
    }

    @Override
    public Rating build() {
        Preconditions.checkState(hasUserId, "no user ID set");
        Preconditions.checkState(hasItemId, "no item ID set");
        Preconditions.checkState(hasRating, "no rating set");
        if (!idSet) {
            if (!hasWarned) {
                logger.warn("creating rating without ID");
                hasWarned = true;
            }
            id = idGenerator.incrementAndGet();
        }
        if (timestamp >= 0) {
            return new Rating.WithTimestamp(id, userId, itemId, rating, timestamp);
        } else {
            return new Rating(id, userId, itemId, rating);
        }
    }
}
