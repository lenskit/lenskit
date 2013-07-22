package org.grouplens.lenskit.data.event;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Build a {@link Rating}.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RatingBuilder implements Builder<Rating> {
    private static final AtomicLong eventIds = new AtomicLong();

    private boolean hasId;
    private long eventId;
    private boolean hasUserId;
    private long userId;
    private boolean hasItemId;
    private long itemId;
    private boolean hasRating;
    private double rating;
    private long timestamp;

    /**
     * Create an uninitialized rating builder.
     */
    public RatingBuilder() {}

    /**
     * Create a rating builder with a particular event ID.
     * @param id The event ID.
     */
    public RatingBuilder(long id) {
        eventId = id;
        hasId = true;
    }

    /**
     * Construct a new rating builder that is a copy of a particular rating.
     * @param r The rating to copy.
     * @return A rating builder that will initially build a copy of the specified rating.
     */
    public static RatingBuilder copy(Rating r) {
        return Ratings.copyBuilder(r);
    }

    /**
     * Get the event ID.
     * @return The event ID.
     */
    public long getId() {
        Preconditions.checkState(hasId, "Event ID must be set");
        return eventId;
    }

    /**
     * Set the event ID.
     * @param id The event ID.
     * @return The builder (for chaining).
     */
    public RatingBuilder setId(long id) {
        eventId = id;
        hasId = true;
        return this;
    }

    /**
     * Set a new unique event ID.  IDs are generated incrementally starting from 1 at the beginning
     * of the application.  This method is really only useful for testing.
     *
     * @return The builder (for chaining).
     */
    public RatingBuilder newId() {
        setId(eventIds.incrementAndGet());
        return this;
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
    public RatingBuilder setItemId(long iid) {
        itemId = iid;
        hasItemId = true;
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
     * @param r The rating value.
     * @return The builder (for chaining).
     */
    public RatingBuilder setRating(double r) {
        rating = r;
        hasRating = true;
        return this;
    }

    /**
     * Clear the rating value (so this builder builds unrate events).
     * @return The builder (for chaining).
     */
    public RatingBuilder clearRating() {
        hasRating = false;
        return this;
    }

    /**
     * Query whether this builder has a rating.
     * @return {@code true} if the builder has a rating, {@code false} if it will produce unrate
     * events.
     */
    public boolean hasRating() {
        return hasRating;
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
    public RatingBuilder setTimestamp(long ts) {
        timestamp = ts;
        return this;
    }

    @Override
    public Rating build() {
        Preconditions.checkState(hasId, "no event ID set");
        Preconditions.checkState(hasUserId, "no user ID set");
        Preconditions.checkState(hasItemId, "no item ID set");
        if (hasRating) {
            return new SimpleRating(eventId,  userId, itemId, rating, timestamp);
        } else {
            return new SimpleNullRating(eventId, userId, itemId, timestamp);
        }
    }
}
