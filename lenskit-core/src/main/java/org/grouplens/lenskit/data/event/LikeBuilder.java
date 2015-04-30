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

import com.google.common.base.Preconditions;

/**
 * Builder for {@link Like} events.
 *
 * @since 2.2
 * @see Like
 * @see Like#newBuilder()
 */
public class LikeBuilder implements EventBuilder<Like> {
    private long userId;
    private long itemId;
    private long timestamp = -1;
    private boolean hasUserId, hasItemId;

    @Override
    public void reset() {
        hasUserId = hasItemId = false;
        timestamp = -1;
    }

    @Override
    public LikeBuilder setUserId(long uid) {
        userId = uid;
        hasUserId = true;
        return this;
    }

    @Override
    public LikeBuilder setItemId(long iid) {
        itemId = iid;
        hasItemId = true;
        return this;
    }

    @Override
    public LikeBuilder setTimestamp(long ts) {
        timestamp = ts;
        return this;
    }

    @Override
    public Like build() {
        Preconditions.checkArgument(hasUserId, "no user ID set");
        Preconditions.checkArgument(hasItemId, "no item ID set");
        return new Like(userId, itemId, timestamp);
    }
}
