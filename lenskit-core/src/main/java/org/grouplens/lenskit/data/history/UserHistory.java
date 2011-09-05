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

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.data.event.Event;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Represents a user profile, associating a list of events with a user. The
 * events are in timestamp order. Histories also can memoize summaries and other
 * computed properties of themselves.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@ThreadSafe
public interface UserHistory<E extends Event> extends List<E> {
    /**
     * Retrieve the user ID.
     *
     * @return The ID of the user who owns this history.
     */
    long getUserId();

    /**
     * Filter the user history to only contain elements of a particular type.
     *
     * @param type The type of elements to include.
     * @return A user history containing only the elements of the specified
     *         type.
     */
    <T extends Event> UserHistory<T> filter(Class<T> type);

    /**
     * Filter the user history with a predicate.
     *
     * @param pred The predicate to with which to filter the history.
     * @return A copy of this user history containing only elements which match
     *         the predicate.
     */
    UserHistory<E> filter(Predicate<? super E> pred);

    /**
     * Get the set of items touched by events in this history. Only nonnegative
     * item IDs are considered.
     *
     * @return The set of all item IDs used in events in this history.
     */
    LongSet itemSet();

    /**
     * Call a function on this history, memoizing its return value. Used for
     * caching things like summaries. The function should appropriately define
     * its {@link Function#equals(Object)} and {@link Object#hashCode()} methods
     * in order for memoization to work well.
     *
     * <p>
     * This method is not synchronized. It is safe to memoize distinct function
     * in parallel, but potentially-parallel use of the same function must be
     * synchronized by client code or the function may be called twice. The
     * implementation in {@link AbstractUserHistory} uses a
     * {@link ConcurrentHashMap}.  Multiple calls are therefore safe, but may
     * result in extra work.  All implementations must maintain this safety
     * guarantee, although they may do so by synchronizing this method.
     *
     * @param func The function to call and memoize.
     */
    <T> T memoize(Function<? super UserHistory<E>, ? extends T> func);
}
