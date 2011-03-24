/*
 * RefLens, a reference implementation of recommender algorithms.
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

import org.grouplens.lenskit.data.LongCursor;

/**
 * DAO for user-item ID data.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface UserItemDataAccessObject {
    /**
     * Retrieve the users from the data source.
     * @return a cursor iterating the user IDs.
     */
    public LongCursor getUsers();

    /**
     * Get the number of users in the system.  This should be the same number
     * of users that will be returned by iterating {@link #getUsers()} (unless
     * a user is added or removed between the two calls).
     * @return The number of users in the system.
     */
    public int getUserCount();

    /**
     * Retrieve the items from the data source.
     * @return a cursor iterating the item IDs.
     */
    public LongCursor getItems();

    /**
     * Get the number of items in the system.  This should be the same number
     * of items that will be returned by iterating {@link #getItems()} (unless
     * an item is added or removed between the two calls).
     * @return The number of items in the system.
     */
    public int getItemCount();
}
