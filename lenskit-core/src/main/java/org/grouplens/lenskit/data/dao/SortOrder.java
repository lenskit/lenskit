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
/**
 *
 */
package org.grouplens.lenskit.data.dao;


import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Events;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public (but may add orders)
 */
public enum SortOrder {
    /**
     * Any order is acceptable.
     */
    ANY {
        @Nullable
        @Override
        public Comparator<Event> getEventComparator() {
            return null;
        }
    },
    /**
     * Sort by timestamp.
     */
    TIMESTAMP {
        @Override
        public Comparator<Event> getEventComparator() {
            return Events.TIMESTAMP_COMPARATOR;
        }
    },
    /**
     * Sort by user, then by timestamp.
     */
    USER {
        @Override
        public Comparator<Event> getEventComparator() {
            return Events.USER_TIME_COMPARATOR;
        }
    },
    /**
     * Sort by item, then by timestamp.
     */
    ITEM {
        public Comparator<Event> getEventComparator() {
            return Events.ITEM_TIME_COMPARATOR;
        }
    };

    /**
     * Get the event comparator for a sort order.
     * @return An appropriate comparator, or {@code null} if the order is unsorted.
     */
    @Nullable
    public abstract Comparator<Event> getEventComparator();
}
