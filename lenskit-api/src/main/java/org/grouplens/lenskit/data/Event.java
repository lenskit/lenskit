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
package org.grouplens.lenskit.data;


/**
 * An event in a user's history. Events are associated with items. In some
 * cases, it makes sense that a user may have events not associated with an item
 * (such as login/logout events). We recommend that integrators use a designated
 * item ID in such cases.  If that item type never shows up in events used by
 * recommenders, then it should not affect the recommenders.
 *
 * <p>
 * Events are immutable. Deviations must be clearly documented and only
 * used in very limited cases (e.g. to implement fast iterators).
 *
 * <p>
 * Implementations must also have well-defined {@link #equals(Object)} and
 * {@link #hashCode()} methods.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface Event {
    /**
     * Get the event's ID. Each event must have a globally-unique ID, and events
     * are considered equal if their IDs are equal.
     *
     * @return The event ID.
     */
    long getId();

    /**
     * Get the ID of the user responsible for the event.
     *
     * @return The event's user ID.
     */
    long getUserId();

    /**
     * Get the item ID.
     *
     * @return The item ID of the event.
     */
    abstract long getItemId();

    /**
     * Get the event timestamp. A timestamp of -1 indicates that the event has
     * no timestamp; such events should generally be considered to happen
     * infinitely long ago.
     *
     * @return The event timestamp, or -1 if there is no timestamp.
     */
    long getTimestamp();

    /**
     * Copy this event.
     *
     * <p> Copied events should generally be independent of any backing store (e.g. an event backed
     * by an index into an array of data should, when copied, create a new event that stores the
     * data directly). As a result, the object returned by this method may be of a different
     * concrete type. They should also always be immutable. If the event is already immutable, it
     * should return itself.
     *
     * @return A copy of the event.
     * @deprecated Copy the event directly using an appropriate builder. This method was an
     *             overly-leaky abstraction.  Events are immutable; if you have a mutable event, it
     *             is from a fast collection, and the client code or iterator implementation is
     *             responsible to know what to do.
     */
    @Deprecated
    Event copy();
}
