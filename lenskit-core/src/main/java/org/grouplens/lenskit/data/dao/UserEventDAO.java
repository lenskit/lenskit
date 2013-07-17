/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;

/**
 * DAO to retrieve events by user.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(StreamingUserEventDAO.class)
public interface UserEventDAO {
    /**
     * Stream events grouped by user.
     *
     * @return A cursor of user histories.
     */
    Cursor<UserHistory<Event>> streamEventsByUser();

    /**
     * Get the events for a specific user.
     * @param user The user ID.
     * @return The user's history, or {@code null} if the user is unknown.
     */
    UserHistory<Event> getEventsForUser(long user);

    /**
     * Get the events for a specific user, filtering by type.
     *
     * @param user The user ID.
     * @param type The type of events to retrieve.
     * @return The user's history, or {@code null} if the user is unknown.
     */
    <E extends Event> UserHistory<E> getEventsForUser(long user, Class<E> type);
}
