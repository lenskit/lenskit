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
 * Basic implementation of {@link Like}.
 */
class SimpleLike implements Like {
    private final long userId;
    private final long itemId;
    private final long timestamp;

    SimpleLike(long uid, long iid, long ts) {
        userId = uid;
        itemId = iid;
        timestamp = ts;
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
