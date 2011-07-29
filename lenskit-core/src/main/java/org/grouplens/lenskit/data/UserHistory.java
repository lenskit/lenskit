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

import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.vector.UserRatingVector;

import com.google.common.base.Predicate;

/**
 * Represents a user profile, associating a list of events with a user. The
 * events are in timestamp order. Histories have special knowledge of some event
 * types; for example, they can extract a rating vector.
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
     * Extract a rating vector from this history. Implementations will generally
     * memoize the vector.
     * 
     * @return A vector mapping item IDs to the user's ratings.
     */
    UserRatingVector ratingVector();
}
