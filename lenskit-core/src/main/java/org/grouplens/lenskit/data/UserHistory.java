/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.data.event.Event;

/**
 * Represents a user profile, associating a list of events with a user.  The
 * events are in timestamp order.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@ThreadSafe
public interface UserHistory<E extends Event> extends List<E> {
    /**
     * Retrieve the user ID.
     * @return The ID of the user who owns this history.
     */
    long getUserId();
}
