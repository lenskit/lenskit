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
package org.grouplens.lenskit.data.dao;

import java.io.Closeable;

import org.grouplens.lenskit.data.LongCursor;

/**
 * DAO for user-item ID data. Each DAO instance represents an open session to
 * the data on the current thread, with similar semantics to a database session.
 * DAO's are opened by using an implementation of the
 * {@link DataAccessObjectFactory}. The DAO's {@link #close()} must be called
 * when the DAO is no longer in use. A DAO is not thread-safe and should only be
 * used on the thread it was created on.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface UserItemDataAccessObject extends Closeable {
    /**
     * Retrieve the users from the data source.
     * @return a cursor iterating the user IDs.
     * @throws NoSessionException if no session is open on the current thread.
     */
    LongCursor getUsers();

    /**
     * Get the number of users in the system.  This should be the same number
     * of users that will be returned by iterating {@link #getUsers()} (unless
     * a user is added or removed between the two calls).
     * @return The number of users in the system.
     * @throws NoSessionException if no session is open on the current thread.
     */
    int getUserCount();

    /**
     * Retrieve the items from the data source.
     * @return a cursor iterating the item IDs.
     */
    LongCursor getItems();

    /**
     * Get the number of items in the system.  This should be the same number
     * of items that will be returned by iterating {@link #getItems()} (unless
     * an item is added or removed between the two calls).
     * @return The number of items in the system.
     * @throws NoSessionException if no session is open on the current thread.
     */
    int getItemCount();
    
    /**
     * Close this DAO so that any underlying data session is closed. The DAO is
     * no longer usable, and a new DAO must be re-opened from a
     * DataAccessObjectFactory.
     */
    public void close();
}
