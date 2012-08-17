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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.grouplens.lenskit.data.dao.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC session, wrapped to provide access to statements, etc.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
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
    private PreparedStatement eventStatements[] =
            new PreparedStatement[SortOrder.values().length];
    private PreparedStatement userEventStatement;
    private PreparedStatement itemEventStatement;

    public JDBCDataSession(Connection dbc, SQLStatementFactory sfac) {
        this(dbc, sfac, true);
    }

    public JDBCDataSession(Connection dbc, SQLStatementFactory sfac, boolean close) {
        connection = dbc;
        closeConnection = close;
        statementFactory = sfac;
    }

    public PreparedStatement userStatement() throws SQLException {
        if (userStatement == null) {
            userStatement = statementFactory.prepareUsers(connection);
        }
        return userStatement;
    }

    public PreparedStatement userCountStatement() throws SQLException {
        if (userCountStatement == null) {
            userCountStatement = statementFactory.prepareUserCount(connection);
        }
        return userCountStatement;
    }

    public PreparedStatement itemStatement() throws SQLException {
        if (itemStatement == null) {
            itemStatement = statementFactory.prepareItems(connection);
        }
        return itemStatement;
    }

    public PreparedStatement itemCountStatement() throws SQLException {
        if (itemCountStatement == null) {
            itemCountStatement = statementFactory.prepareItemCount(connection);
        }
        return itemCountStatement;
    }

    public PreparedStatement eventStatement(SortOrder order) throws SQLException {
        int o = order.ordinal();
        if (eventStatements[o] == null) {
            eventStatements[o] = statementFactory.prepareEvents(connection, order);
        }
        return eventStatements[o];
    }

    public PreparedStatement userEventStatement() throws SQLException {
        if (userEventStatement == null) {
            userEventStatement = statementFactory.prepareUserEvents(connection);
        }
        return userEventStatement;
    }

    public PreparedStatement itemEventStatement() throws SQLException {
        if (itemEventStatement == null) {
            itemEventStatement = statementFactory.prepareItemEvents(connection);
        }
        return itemEventStatement;
    }

    private boolean closeStatement(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
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
            for (PreparedStatement s : eventStatements) {
                failed = failed || !closeStatement(s);
            }
            failed = failed || !closeStatement(userEventStatement);
            failed = failed || !closeStatement(itemEventStatement);
            if (closeConnection) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
        if (failed) {
            throw new IOException("Error closing statement (see log for details)");
        }
    }

}
