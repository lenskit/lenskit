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
package org.grouplens.lenskit.data.text;

import org.grouplens.lenskit.data.event.Event;

/**
 * Description of how an event is laid out in a line in a text file.
 * <p>
 * In addition to a plain parsing method, this class provides some additional methods to
 * allow objects and/or builders to be reused for more efficient data file parsing.
 * </p>
 */
public interface EventFormat {
    /**
     * Return the type of event that this class returns.
     * @return The event type returned by this class.
     */
    Class<? extends Event> getEventType();

    /**
     * Parse a line into an event.
     *
     * @param line The line to parse.
     * @return The event resulting from parsing the line.
     */
    Event parse(String line) throws InvalidRowException;

    /**
     * Create a new context that may speed up parsing.  This will be passed to {@link #parse(String, Object)};
     * it may be a builder to be reused, or a mutable object.
     * <p>
     * The context will only ever be used from one thread.
     * </p>
     *
     * @return The context.
     */
    Object newContext();

    /**
     * Parse using a context.  It is permitted for {@code context} to be a mutable event, and for
     * this method to return that event.  Cursors using this format will use {@link #copy(Event)}
     * to copy the event if needed.  This method will only be used without {@code copy} in order to
     * provide fast iteration.
     *
     * @param line The line to parse.
     * @param context The parsing context.
     * @return
     */
    Event parse(String line, Object context) throws InvalidRowException;

    /**
     * Copy an event.  This only needs to copy the event if events returned by this layout are
     * mutable.
     *
     * @param evt The event to copy.
     * @return A copy of the event (can be the same event if events are not mutable).
     */
    Event copy(Event evt);
}
