/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.grouplens.lenskit.data.event.Event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Basic user rating profile backed by a collection of ratings. The event list
 * aspects of the profile are implemented as a read-only delegation to an
 * underlying list.
 *
 * @see History#forUser(long, List)
 * @param <E> The type of event in this history.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BasicUserHistory<E extends Event> extends AbstractUserHistory<E> implements UserHistory<E> {
    private final long user;
    private final ImmutableList<E> events;

    /**
     * Construct a new basic user profile.
     *
     * @param u  The user ID.
     * @param es The list of events in this user's history.  All events must
     *           be for the user.  The list is defensively copied to preserve
     *           immutability (using {@link ImmutableList#copyOf(java.util.Collection)},
     *           so immutable lists are used as-is).  The list must already be sorted.
     */
    @SuppressWarnings("unchecked")
    BasicUserHistory(long u, List<? extends E> es) {
        user = u;
        events = ImmutableList.copyOf(es);
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
        return events.subList(from, to);
    }

    @Override
    public Object[] toArray() {
        return events.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return events.toArray(a);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation filters into a new {@link BasicUserHistory} backed
     * by an {@link ArrayList}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> UserHistory<T> filter(Class<T> type) {
        // pre-scan the history to see if we need to copy
        if (Iterables.all(this, Predicates.instanceOf(type))) {
            return (UserHistory<T>) this;
        } else {
            List<T> evts = ImmutableList.copyOf(Iterables.filter(this, type));
            return new BasicUserHistory<T>(getUserId(), evts);
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation filters into a new {@link BasicUserHistory} backed
     * by an {@link ArrayList}.
     */
    @Override
    public UserHistory<E> filter(Predicate<? super E> pred) {
        if (Iterables.all(this, pred)) {
            return this;
        } else {
            List<E> evts = ImmutableList.copyOf(Iterables.filter(this, pred));
            return new BasicUserHistory<E>(getUserId(), evts);
        }
    }
}
