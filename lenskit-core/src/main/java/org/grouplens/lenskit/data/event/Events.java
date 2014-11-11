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

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Longs;

import java.util.Comparator;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Events {
    private Events() {}

    /**
     * Compare two events by timestamp.
     */
    public static final Comparator<Event> TIMESTAMP_COMPARATOR = new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            return Longs.compare(e1.getTimestamp(), e2.getTimestamp());
        }
    };

    /**
     * Compare two events by user, then timestamp.
     */
    public static final Comparator<Event> USER_TIME_COMPARATOR = new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            return ComparisonChain.start()
                                  .compare(e1.getUserId(), e2.getUserId())
                                  .compare(e1.getTimestamp(), e2.getTimestamp())
                                  .result();
        }
    };

    /**
     * Compare two item events by item, then timestamp.
     */
    public static final Comparator<Event> ITEM_TIME_COMPARATOR = new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            return ComparisonChain.start()
                                  .compare(e1.getItemId(), e2.getItemId())
                                  .compare(e1.getTimestamp(), e2.getTimestamp())
                                  .result();
        }
    };

    /**
     * Create a new {@link Like} event with no timestamp.
     *
     * @param user The user ID.
     * @param item The item ID.
     * @return A {@link Like}.
     */
    public static Like like(long user, long item) {
        return like(user, item, -1);
    }

    /**
     * Create a new {@link Like} event.
     *
     * @param user The user ID.
     * @param item The item ID.
     * @param ts The timestamp.
     * @return A {@link Like}.
     */
    public static Like like(long user, long item, long ts) {
        return new SimpleLike(user, item, ts);
    }

    /**
     * Create a {@link LikeBatch} event with a nonzero count.
     * @param user The user ID.
     * @param item The item ID.
     * @param count The count.
     * @return The new event.
     */
    public static LikeBatch likeBatch(long user, long item, int count) {
        return new SimpleLikeBatch(user, item, count);
    }
}
