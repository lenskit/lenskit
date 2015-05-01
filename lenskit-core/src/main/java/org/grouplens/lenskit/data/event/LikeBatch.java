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

/**
 * Generic interface representing a batch of unary interactions.  Events of this type will generally
 * not have timestamps.  An example of a use of this event type is to represent play counts in a
 * summarized data set.
 * <p>
 * Like batches can be constructed with {@link LikeBatchBuilder} or {@link #create(long, long, int)}.
 *
 * @since 2.2
 * @see Like
 */
public class LikeBatch implements Event {
    private final long userId;
    private final long itemId;
    private final int count;

    LikeBatch(long uid, long iid, int ct) {
        userId = uid;
        itemId = iid;
        count = ct;
    }

    public static LikeBatchBuilder newBuilder() {
        return new LikeBatchBuilder();
    }

    public static LikeBatch create(long user, long item, int count) {
        return new LikeBatch(user, item, count);
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public long getItemId() {
        return itemId;
    }

    /**
     * Get the number of times the item has been liked.
     * @return The number of times the item has been liked.
     */
    public int getCount() {
        return count;
    }

    @Override
    public long getTimestamp() {
        return -1;
    }

    /**
     * Create a new builder initialized with a copy of this event.
     * @return A new {@link LikeBatchBuilder} initialized with a copy of this event's data.
     */
    public LikeBatchBuilder copyBuilder() {
        return newBuilder().setUserId(userId)
                           .setItemId(itemId)
                           .setCount(count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeBatch)) return false;

        LikeBatch that = (LikeBatch) o;

        if (count != that.getCount()) return false;
        if (itemId != that.getItemId()) return false;
        if (userId != that.getUserId()) return false;
        if (that.getTimestamp() != -1) return false; //other implementations may have timestamps

        return true;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        hcb.append(userId)
           .append(itemId)
           .append(count)
           .append(-1L);
        return hcb.toHashCode();
    }

    @Override
    public String toString() {
        return "LikeBatch{" +
                "userId=" + userId +
                ", itemId=" + itemId +
                ", count=" + count +
                '}';
    }
}
