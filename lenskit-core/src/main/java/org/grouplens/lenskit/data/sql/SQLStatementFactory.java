/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.SortOrder;

/**
 * Interface for producing prepared statements for rating DAO queries.
 *
 * <p>
 * Methods on this interface prepare SQL statements to be used by
 * {@link JDBCDataSession} to satisfy queries. The data session implementation
 * takes care of caching prepared statements, so these methods should always
 * prepare new statements.
 *
 * <p>
 * The JDBC DAO framework operates by expecting a statement factory to construct
 * queries which return results in a defined format.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@Immutable
public interface SQLStatementFactory {
    /**
     * Prepare a statement to satisfy {@link DataAccessObject#getUsers()}.
     * Querying the statement should return one column per row containing the
     * numeric user ID.
     *
     * @return A {@code PreparedStatement} containing user ID data.
     */
    PreparedStatement prepareUsers(Connection dbc) throws SQLException;

    /**
     * Prepare a statement to satisfy {@link DataAccessObject#getUserCount()}.
     * The result set should contain a single row whose first column contains
     * the number of users.
     *
     * @return A {@code PreparedStatement} containing the total number of
     *         users.
     */
    PreparedStatement prepareUserCount(Connection dbc) throws SQLException;

    /**
     * Prepare a statement to satisfy {@link DataAccessObject#getItems()}.
     * Querying the statement should return one column per row containing the
     * numeric item ID.
     *
     * @return A {@code PreparedStatement} containing item ID data.
     */
    PreparedStatement prepareItems(Connection dbc) throws SQLException;

    /**
     * Prepare a statement to satisfy {@link DataAccessObject#getItemCount()}.
     * The result set should contain a single row whose first column contains
     * the number of items.
     *
     * @return A {@code PreparedStatement} containing the total number of
     *         items.
     */
    PreparedStatement prepareItemCount(Connection dbc) throws SQLException;

    /**
     * Prepare a statement to satisfy
     * {@link DataAccessObject#getEvents(SortOrder)}. Each row should contain
     * four or five columns: the event ID, the user ID, the item ID, the rating,
     * and (optionally) the timestamp. The timestamp column is allowed to
     * contain NULL values or to be omitted entirely. ID, user, item, and rating
     * columns must be non-null.
     *
     * @param dbc   The database connection
     * @param order The sort order
     */
    PreparedStatement prepareEvents(Connection dbc, SortOrder order)
            throws SQLException;

    /**
     * Prepare a statement to satisfy
     * {@link DataAccessObject#getUserEvents(long)}. The returned rows should be
     * as in {@link #prepareEvents(Connection, SortOrder)}, and the prepared
     * statement should take a single parameter for the user ID.
     *
     * @param dbc
     * @return A {@code PreparedStatement} returning user rating data. The
     *         ratings must be in timestamp order.
     */
    PreparedStatement prepareUserEvents(Connection dbc) throws SQLException;

    /**
     * Prepare a statement to satisfy
     * {@link DataAccessObject#getItemEvents(long)}. The returned rows should be
     * as in {@link #prepareEvents(Connection, SortOrder)}, and the prepared
     * statement should take a single parameter for the item ID.
     *
     * @param dbc
     * @return A {@code PreparedStatement} returning item rating data. The
     *         ratings must be ordered first by user ID, then by timestamp.
     */
    PreparedStatement prepareItemEvents(Connection dbc) throws SQLException;
}
