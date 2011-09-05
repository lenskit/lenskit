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
package org.grouplens.lenskit.data.history;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.grouplens.lenskit.data.event.Event;

/**
 * Basic user rating profile backed by a collection of ratings. The event list
 * aspects of the profile are implemented as a read-only delegation to an
 * underlying list.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BasicUserHistory<E extends Event> extends AbstractUserHistory<E> implements UserHistory<E> {
    private long user;
    private List<E> events;

    /**
     * Construct a new basic user profile.
     *
     * @param user The user ID.
     * @param events The list of events in this user's history. All events must
     *            be for the user.
     */
    public BasicUserHistory(long user, List<E> events) {
        this.user = user;
        this.events = events;
    }

    @Override
    public long getUserId() {
        return user;
    }

    @Override
    public E get(int i) {
        return events.get(i);
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public Iterator<E> iterator() {
        return events.iterator();
    }

    @Override
    public List<E> subList(int from, int to) {
        return Collections.unmodifiableList(events.subList(from, to));
    }

    @Override
    public Object[] toArray() {
        return events.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return events.toArray(a);
    }
}
