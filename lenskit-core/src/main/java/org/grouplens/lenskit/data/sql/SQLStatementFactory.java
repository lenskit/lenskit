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
package org.grouplens.lenskit.data.sql;

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.data.dao.SortOrder;

/**
 * Interface for producing sql strings rating DAO queries.
 *
 * <p>
 * The JDBC DAO framework operates by expecting a statement factory to construct
 * queries which return results in a defined format.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Immutable
public interface SQLStatementFactory {
    /**
     * Prepare a statement to satisfy {@link org.grouplens.lenskit.data.dao.UserDAO#getUserIds()}.
     * Querying the statement should return one column per row containing the
     * numeric user ID.
     *
     * @return A string for a sql query containing user ID data.
     */
    String prepareUsers();

    /**
     * Prepare a statement to satisfy {@link org.grouplens.lenskit.data.dao.ItemDAO#getItemIds()}.
     * Querying the statement should return one column per row containing the
     * numeric item ID.
     *
     * @return A string for a sql query containing item ID data.
     */
    String prepareItems();

    /**
     * Prepare a statement to satisfy
     * {@link org.grouplens.lenskit.data.dao.EventDAO#streamEvents(Class, SortOrder)}. Each row should contain
     * three or four columns: the user ID, the item ID, the rating,
     * and (optionally) the timestamp. The timestamp column is allowed to
     * contain NULL values or to be omitted entirely. User, item, and rating
     * columns must be non-null.
     *
     * @param order The sort order
     */
    String prepareEvents(SortOrder order);

    /**
     * Prepare a statement to satisfy
     * {@link org.grouplens.lenskit.data.dao.UserEventDAO#getEventsForUser(long)}. The returned rows should be
     * as in {@link #prepareEvents(SortOrder)}, and the prepared
     * statement should take a single parameter for the user ID.
     *
     * @return A string for a sql query returning user rating data. The ratings
     *         must be in timestamp order.
     */
    String prepareUserEvents();

    /**
     * Prepare a statement to satisfy {@link org.grouplens.lenskit.data.dao.ItemEventDAO#getEventsForItem(long)}.
     * The returned rows should be as in {@link #prepareEvents(SortOrder)}, and the prepared
     * statement should take a single parameter for the item ID.
     *
     * @return A string for a sql query returning item rating data. The ratings must be ordered
     *         first by user ID, then by timestamp.
     */
    String prepareItemEvents();

    /**
     * Prepare a statement to satisfy {@link org.grouplens.lenskit.data.dao.ItemEventDAO#getUsersForItem(long)}.
     * The returned rows should each contain a user ID as their only column, and the statement
     * should take a single parameter for the item ID.
     *
     * @return A string for a sql query returning item rating data. The ratings must be ordered
     *         first by user ID, then by timestamp.
     */
    String prepareItemUsers();
}
