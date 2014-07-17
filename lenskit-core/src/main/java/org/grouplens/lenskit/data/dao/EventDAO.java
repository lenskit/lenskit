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
package org.grouplens.lenskit.data.dao;

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.event.Event;

/**
 * Basic interface for accessing events.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface EventDAO {
    /**
     * Stream all events.
     *
     * @return A cursor over all events.
     */
    Cursor<Event> streamEvents();

    /**
     * Stream all events of a given type.
     *
     * @param type The event type.
     * @return A cursor over all events.
     */
    <E extends Event> Cursor<E> streamEvents(Class<E> type);

    /**
     * Stream all events of a given type in a specified order.
     *
     * @param type The event type.
     * @param order The order.
     * @return A cursor over all events.
     */
    <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order);
}
