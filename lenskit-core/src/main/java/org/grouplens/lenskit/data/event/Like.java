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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Generic interface representing a unary interaction.  This provides a basic, generic representation
 * of likes, clicks, purchases, and other unary interactions.  Applications may want to implement
 * their own events to represent such interactions.
 * <p>
 * This class can be created with {@link LikeBuilder} or the {@link #create(long, long, long)} method.
 * </p>
 *
 * @since 2.2
 * @see Events#like(long, long)
 * @see Events#like(long, long, long)
 */
public final class Like implements Event, Serializable {
    private static final long serialVersionUID = 1L;

    private final long userId;
    private final long itemId;
    private final long timestamp;

    Like(long uid, long iid, long ts) {
        userId = uid;
        itemId = iid;
        timestamp = ts;
    }

    /**
     * Create a new like builder.
     * @return A new builder for Like events.
     */
    public static LikeBuilder newBuilder() {
        return new LikeBuilder();
    }

    /**
     * Create a new {@link Like} event with no timestamp.
     *
     * @param user The user ID.
     * @param item The item ID.
     * @return A {@link Like}.
     */
    public static Like create(long user, long item) {
        return create(user, item, -1);
    }

    /**
     * Create a new {@link Like} event.
     *
     * @param user The user ID.
     * @param item The item ID.
     * @param ts The timestamp.
     * @return A {@link Like}.
     */
    public static Like create(long user, long item, long ts) {
        return new Like(user, item, ts);
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getItemId() {
        return itemId;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Create a new builder initialized with a copy of this event's contents.
     * @return A new builder initialized with this event's data.
     */
    public LikeBuilder copyBuilder() {
        return newBuilder().setUserId(userId)
                           .setItemId(itemId)
                           .setTimestamp(timestamp);
    }

    @Override
    public String toString() {
        return "Like{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Like)) return false;

        Like that = (Like) o;

        if (itemId != that.getItemId()) return false;
        if (timestamp != that.getTimestamp()) return false;
        if (userId != that.getUserId()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(userId)
           .append(itemId)
           .append(timestamp);
        return hcb.toHashCode();
    }
}
