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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.grouplens.lenskit.data.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class JDBCDataSession implements Closeable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    final Connection connection;
    final boolean closeConnection;
    
    private final SQLStatementFactory statementFactory;
    
    private PreparedStatement userStatement;
    private PreparedStatement userCountStatement;
    private PreparedStatement itemStatement;
    private PreparedStatement itemCountStatement;
    private PreparedStatement ratingStatements[] =
    	new PreparedStatement[SortOrder.values().length];
    private PreparedStatement userRatingStatements[] =
    	new PreparedStatement[SortOrder.values().length];
    private PreparedStatement itemRatingStatements[] =
    	new PreparedStatement[SortOrder.values().length];
    
    public JDBCDataSession(Connection dbc, SQLStatementFactory sfac) {
        this(dbc, sfac, true);
    }
    
    public JDBCDataSession(Connection dbc, SQLStatementFactory sfac, boolean close) {
        connection = dbc;
        closeConnection = close;
        statementFactory = sfac;
    }
    
    public PreparedStatement userStatement() throws SQLException {
    	if (userStatement == null)
    		userStatement = statementFactory.prepareUsers(connection);
    	return userStatement;
    }
    
    public PreparedStatement userCountStatement() throws SQLException {
    	if (userCountStatement == null)
    		userCountStatement = statementFactory.prepareUserCount(connection);
    	return userCountStatement;
    }
    
    public PreparedStatement itemStatement() throws SQLException {
    	if (itemStatement == null)
    		itemStatement = statementFactory.prepareItems(connection);
    	return itemStatement;
    }
    
    public PreparedStatement itemCountStatement() throws SQLException {
    	if (itemCountStatement == null)
    		itemCountStatement = statementFactory.prepareItemCount(connection);
    	return itemCountStatement;
    }
    
    public PreparedStatement ratingStatement(SortOrder order) throws SQLException {
    	int o = order.ordinal();
    	if (ratingStatements[o] == null)
    		ratingStatements[o] = statementFactory.prepareRatings(connection, order);
    	return ratingStatements[o];
    }
    
    public PreparedStatement userRatingStatement(SortOrder order) throws SQLException {
    	int o = order.ordinal();
    	if (userRatingStatements[o] == null)
    		userRatingStatements[o] = statementFactory.prepareUserRatings(connection, order);
    	return userRatingStatements[o];
    }
    
    public PreparedStatement itemRatingStatement(SortOrder order) throws SQLException {
    	int o = order.ordinal();
    	if (itemRatingStatements[o] == null)
    		itemRatingStatements[o] = statementFactory.prepareItemRatings(connection, order);
    	return itemRatingStatements[o];
    }
    
    private boolean closeStatement(Statement s) {
        try {
            if (s != null)
                s.close();
            return true;
        } catch (SQLException e) {
            logger.error("Error closing statement: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Close the connection and all open statements.
     */
    @Override
    public void close() throws IOException {
        boolean failed = false;
        try {
            failed = failed || !closeStatement(userStatement);
            failed = failed || !closeStatement(userCountStatement);
            failed = failed || !closeStatement(itemStatement);
            failed = failed || !closeStatement(itemCountStatement);
            for (PreparedStatement s: ratingStatements)
                failed = failed || !closeStatement(s);
            for (PreparedStatement s: userRatingStatements)
                failed = failed || !closeStatement(s);
            for (PreparedStatement s: itemRatingStatements)
                failed = failed || !closeStatement(s);
            if (closeConnection)
                connection.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
        if (failed)
            throw new IOException("Error closing statement (see log for details)");
    }

}
