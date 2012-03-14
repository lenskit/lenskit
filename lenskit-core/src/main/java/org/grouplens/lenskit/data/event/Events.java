/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

import org.grouplens.lenskit.data.Event;

import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Longs;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Events {
    /**
     * Integer to generate sequential IDs for fresh events.  Used mostly in
     * test cases.
     */
    static AtomicLong nextEventId = new AtomicLong();

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

}
