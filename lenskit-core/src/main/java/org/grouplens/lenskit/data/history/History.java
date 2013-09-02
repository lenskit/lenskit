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
package org.grouplens.lenskit.data.history;

import org.grouplens.lenskit.data.event.Event;

import java.util.List;

/**
 * Utility methods for user histories.
 *
 * @since 1.3
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class History {
    private History() {}

    /**
     * Create a history for a particular user.
     * @param id The user ID.
     * @param events The events.
     * @param <E> The root type of the events in the history.
     * @return A history object.
     */
    @SuppressWarnings("deprecation")
    public static <E extends Event> UserHistory<E> forUser(long id, List<? extends E> events) {
        return new BasicUserHistory<E>(id, events);
    }
}
