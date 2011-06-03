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
package org.grouplens.lenskit.data.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.annotation.concurrent.Immutable;

import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;

/**
 * Interface for producing prepared statements for rating DAO queries.
 * 
 * <p>
 * Methods on this interface prepare SQL statements to be used by {@link JDBCDataSession}
 * to satisfy queries.  The data session implementation takes care of caching
 * prepared statements, so these methods should always prepare new statements.
 * 
 * <p>The JDBC DAO framework operates by expecting a statement factory to construct
 * queries which return results in a defined format.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
public interface SQLStatementFactory {
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getUsers()}.
     * Querying the statement should return one column per row containing the
     * numeric user ID.
     * @return A <tt>PreparedStatement</tt> containing user ID data.
     */
    PreparedStatement prepareUsers(Connection dbc) throws SQLException;
    
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getUserCount()}.
     * The result set should contain a single row whose first column contains
     * the number of users.
     * @return A <tt>PreparedStatement</tt> containing the total number of users.
     */
    PreparedStatement prepareUserCount(Connection dbc) throws SQLException;
    
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getItems()}.
     * Querying the statement should return one column per row containing the
     * numeric item ID.
     * @return A <tt>PreparedStatement</tt> containing item ID data.
     */
    PreparedStatement prepareItems(Connection dbc) throws SQLException;
    
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getItemCount()}.
     * The result set should contain a single row whose first column contains
     * the number of items.
     * @return A <tt>PreparedStatement</tt> containing the total number of items.
     */
    PreparedStatement prepareItemCount(Connection dbc) throws SQLException;
    
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getRatings(SortOrder)}.
     * Each row should contain three or four columns: the user ID, the item ID,
     * the rating, and (optionally) the timestamp. The timestamp column is allowed
     * to contain NULL values or to be omitted entirely. User, item, and rating
     * columns must be non-null.
     * 
     * @param dbc The database connection
     * @param order The sort order
     */
    PreparedStatement prepareRatings(Connection dbc, SortOrder order) throws SQLException;
    
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getUserRatings(long, SortOrder)}.
     * The returned rows should be as in {@link #prepareRatings(Connection, SortOrder)},
     * and the prepared statement should take a single parameter for the user ID.
     * @param dbc
     * @param order
     * @return A <tt>PreparedStatement</tt> containing user rating data.
     */
    PreparedStatement prepareUserRatings(Connection dbc, SortOrder order) throws SQLException;
    
    /**
     * Prepare a statement to satisfy {@link RatingDataAccessObject#getItemRatings(long, SortOrder)}.
     * The returned rows should be as in {@link #prepareRatings(Connection, SortOrder)},
     * and the prepared statement should take a single parameter for the item ID.
     * @param dbc
     * @param order
     * @return A <tt>PreparedStatement</tt> containing item rating data.
     */
    PreparedStatement prepareItemRatings(Connection dbc, SortOrder order) throws SQLException;
}
