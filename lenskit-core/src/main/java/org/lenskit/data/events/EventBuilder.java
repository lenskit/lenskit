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
package org.lenskit.data.events;

import org.lenskit.data.ratings.RatingBuilder;

/**
 * Interface for common behavior for event builders.
 *
 * @param <E> The type of event built.
 * @since 2.2
 * @see RatingBuilder
 */
public interface EventBuilder<E extends Event> {
    /**
     * Clear the rating builder, as if it were freshly instantiated..
     */
    void reset();

    /**
     * Set the user ID for this event.
     *
     * @param uid The user ID.
     * @return The event builder (for chaining).
     */
    EventBuilder<E> setUserId(long uid);

    /**
     * Set the item ID for this event.
     *
     * @param iid The item ID.
     * @return The event builder (for chaining).
     */
    EventBuilder<E> setItemId(long iid);

    /**
     * Set the timestamp for this event.
     *
     * @param ts The event timestamp.
     * @return The event builder (for chaining).
     */
    EventBuilder<E> setTimestamp(long ts);

    /**
     * Builds the event.  Built event objects are entirely independent of the event builder; the
     * setters can be used to build a new event after this builder has built one event.
     *
     * @return A new event.
     * @throws IllegalStateException if the builder is not ready to build an object (e.g. some
     *                               needed setter has not been called).
     */
    E build();
}
