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
package org.grouplens.lenskit.data;

import java.sql.ResultSet;

import org.grouplens.lenskit.data.context.BuildContext;

import com.google.inject.Provider;

/**
 * DAO for user-item ID data.
 *
 * <p>LensKit uses <a href="http://java.sun.com/blueprints/corej2eepatterns/Patterns/DataAccessObject.html">Data Access Objects</a>
 * to obtain access to rating data.  These objects allow LensKit to query for
 * users, items, ratings, etc.  Some DAOs also support registering for notification
 * of changes.  The DAO should generally be a singleton, therefore, to support change
 * notification and registration throughout the system.
 * 
 * <p>Also, it will often be desirable for cursors to share some backing resource
 * (e.g. all cursors created in a request to be served from the same database
 * connection).  This can be facilitated in a couple of ways:
 * 
 * <ol>
 * <li>Use thread-local
 * storage and reference counting to close the database connection when all cursors
 * have been closed.
 * <li>Inject a {@link Provider} of request-scoped database connections into
 * the DAO implementation, then use its {@link Provider#get()} method to access
 * the database connection in order to set up the cursor (which, in a JDBC
 * implementation, will generally wrap a {@link ResultSet}).  The surrounding
 * request-scoping framework can then take care of closing the database connection.
 * In a web environment, cursors shouldn't survive requests anyway.
 * </ol>
 * 
 * <p>The data access object makes no transactional or immutability guarantees,
 * and does not provide mutation.  An implementation is, of course, free to
 * provide mutation.  The recommender building process uses a {@link BuildContext}
 * so that it can make multiple passes over a snapshot of the data.
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
