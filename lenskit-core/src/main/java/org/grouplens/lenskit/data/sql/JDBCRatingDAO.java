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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.*;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.History;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillCloseWhenClosed;
import javax.inject.Inject;

/**
 * Rating DAO backed by a JDBC connection.  This DAO can only store rating data;
 * no other events are supported.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class JDBCRatingDAO implements EventDAO, UserEventDAO, ItemEventDAO, UserDAO, ItemDAO {
    /**
     * Event ID column number.
     */
    public static final int COL_EVENT_ID = 1;
    /**
     * User ID column number.
     */
    public static final int COL_USER_ID = 2;
    /**
     * Item ID column number.
     */
    public static final int COL_ITEM_ID = 3;
    /**
     * Rating column number.
     */
    public static final int COL_RATING = 4;
    /**
     * Timestamp column number.
     */
    public static final int COL_TIMESTAMP = 5;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Connection connection;
    protected final boolean closeConnection;

    private final SQLStatementFactory statementFactory;

    private final CachedPreparedStatement userStatement;
    private final CachedPreparedStatement userCountStatement;
    private final CachedPreparedStatement itemStatement;
    private final CachedPreparedStatement itemCountStatement;
    private final Map<SortOrder,CachedPreparedStatement> eventStatements =
            new EnumMap<SortOrder,CachedPreparedStatement>(SortOrder.class);
    private final CachedPreparedStatement userEventStatement;
    private final CachedPreparedStatement itemEventStatement;
    private final CachedPreparedStatement itemUserStatement;

    /**
     * Create a new JDBC rating DAO.
     *
     * @param dbc  The database connection. The connection will be closed
     *             when the DAO is closed.
     * @param sfac The statement factory.
     */
    @Inject
    public JDBCRatingDAO(@WillCloseWhenClosed Connection dbc, SQLStatementFactory sfac) {
        this(dbc, sfac, true);
    }

    /**
     * Create a new JDBC rating DAO.
     *
     * @param dbc   The database connection.
     * @param sfac  The statement factory.
     * @param close Whether to close the database connection when the DAO is closed.
     */
    public JDBCRatingDAO(Connection dbc, SQLStatementFactory sfac, boolean close) {
        connection = dbc;
        closeConnection = close;
        statementFactory = sfac;
        userStatement = new CachedPreparedStatement(dbc, statementFactory.prepareUsers());
        userCountStatement = new CachedPreparedStatement(dbc, statementFactory.prepareUserCount());
        itemStatement = new CachedPreparedStatement(dbc, statementFactory.prepareItems());
        itemCountStatement = new CachedPreparedStatement(dbc, statementFactory.prepareItemCount());
        for (SortOrder order : SortOrder.values()) {
            eventStatements.put(order, new CachedPreparedStatement(dbc,
                statementFactory.prepareEvents(order)));
        }
        userEventStatement = new CachedPreparedStatement(dbc, statementFactory.prepareUserEvents());
        itemEventStatement = new CachedPreparedStatement(dbc, statementFactory.prepareItemEvents());
        itemUserStatement = new CachedPreparedStatement(dbc, statementFactory.prepareItemUsers());
    }

    private boolean closeStatement(CachedPreparedStatement s) {
        try {
            s.close();
            return true;
        } catch (IOException e) {
            logger.error("Error closing statement: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Close the connection and all open statements.
     */
    public void close() {
        boolean failed = false;
        try {
            failed = failed || !closeStatement(userStatement);
            failed = failed || !closeStatement(userCountStatement);
            failed = failed || !closeStatement(itemStatement);
            failed = failed || !closeStatement(itemCountStatement);
            for (CachedPreparedStatement s : eventStatements.values()) {
                failed = failed || !closeStatement(s);
            }
            failed = failed || !closeStatement(userEventStatement);
            failed = failed || !closeStatement(itemEventStatement);
            if (closeConnection) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
        if (failed) {
            throw new DatabaseAccessException("Error closing statement (see log for details)");
        }
    }

    protected LongSet getIdSet(PreparedStatement s) throws SQLException {
        ResultSet results = s.executeQuery();
        try {
            LongSet ids = new LongOpenHashSet();
            while (results.next()) {
                ids.add(results.getLong(1));
            }
            return ids;
        } finally {
            results.close();
        }
    }

    @Override
    public LongSet getUserIds() {
        try {
            return getIdSet(userStatement.call());
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    @Override
    public LongSet getItemIds() {
        try {
            return getIdSet(itemStatement.call());
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Cursor<Event> streamEvents() {
        return (Cursor) streamEvents(Rating.class, SortOrder.ANY);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return streamEvents(type, SortOrder.ANY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        if (!type.isAssignableFrom(Rating.class)) {
            return Cursors.empty();
        }

        try {
            return (Cursor<E>) new ResultSetRatingCursor(eventStatements.get(order).call());
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    @Override
    public UserHistory<Event> getEventsForUser(long userId) {
        // FIXME Cache this
        try {
            PreparedStatement s = userEventStatement.call();
            s.setLong(1, userId);
            Cursor<Rating> ratings = new ResultSetRatingCursor(s);
            try {
                List<Event> events = ImmutableList.<Event>copyOf(ratings);
                if (events.isEmpty()) {
                    return null;
                } else {
                    return History.forUser(userId, events);
                }
            } finally {
                ratings.close();
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> UserHistory<E> getEventsForUser(long uid, Class<E> type) {
        if (type.isAssignableFrom(Rating.class)) {
            return (UserHistory<E>) getEventsForUser(uid);
        } else {
            return null;
        }
    }

    @Override
    public List<Event> getEventsForItem(long itemId) {
        // FIXME Cache this
        try {
            PreparedStatement s = itemEventStatement.call();
            s.setLong(1, itemId);
            Cursor<Rating> ratings = new ResultSetRatingCursor(s);
            try {
                List<Event> events = ImmutableList.<Event>copyOf(ratings);
                if (events.isEmpty()) {
                    return null;
                } else {
                    return events;
                }
            } finally {
                ratings.close();
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> List<E> getEventsForItem(long iid, Class<E> type) {
        if (type.isAssignableFrom(Rating.class)) {
            return (List<E>) getEventsForItem(iid);
        } else {
            return null;
        }
    }

    @Override
    public LongSet getUsersForItem(long item) {
        PreparedStatement s = null;
        try {
            s = itemUserStatement.call();
            s.setLong(1, item);
            return getIdSet(s);
        } catch (SQLException e) {
            throw new DatabaseAccessException(e);
        }
    }

    @Override
    public Cursor<UserHistory<Event>> streamEventsByUser() {
        return new UserHistoryCursor<Event>(streamEvents(Event.class, SortOrder.USER));
    }
}
