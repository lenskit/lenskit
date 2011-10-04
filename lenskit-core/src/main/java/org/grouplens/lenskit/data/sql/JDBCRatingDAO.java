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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.grouplens.lenskit.cursors.AbstractLongCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.cursors.LongCursor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.dao.AbstractDataAccessObject;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.SortOrder;
import org.grouplens.lenskit.data.event.AbstractRatingCursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.util.MoreFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating DAO backed by a JDBC connection.  This DAO can only store rating data;
 * no other events are supported.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class JDBCRatingDAO extends AbstractDataAccessObject {
    public static final int COL_EVENT_ID = 1;
    public static final int COL_USER_ID = 2;
    public static final int COL_ITEM_ID = 3;
    public static final int COL_RATING = 4;
    public static final int COL_TIMESTAMP = 5;

    /**
     * Factory for creating JDBC DAOs. If the underlying database may be
     * modified by other processes while a build is running, this factory can be
     * configured to take an in-memory snapshot of the database (see
     * {@link #setSnapshotting(boolean)}). By default, this is <tt>false</tt>
     * and the database is assumed to be immutable.
     */
    public static class Factory implements DAOFactory {
        private final String cxnUrl;
        private final SQLStatementFactory factory;
        private volatile boolean takeSnapshot = false;
        private final Properties properties;
        
        public Factory(String url, SQLStatementFactory config) {
            this(url, config, null);
        }

        public Factory(String url, SQLStatementFactory config, Properties props) {
            cxnUrl = url;
            factory = config;
            properties = props;
        }

        /**
         * Query whether this factory takes in-memory snapshots.
         *
         * @return <tt>true</tt> if the factory is configured to take in-memory
         *         snapshots of the database.
         */
        public boolean isSnapshotting() {
            return takeSnapshot;
        }

        /**
         * Set whether the {@link #snapshot()} method should take a snapshot or
         * just return a collection. If the database may be modified by other
         * code while open, set this to <tt>true</tt>.
         *
         * @param take Whether to take an in-memory snapshot of the database in
         *            the {@link #snapshot()} method.
         */
        public Factory setSnapshotting(boolean take) {
            takeSnapshot = take;
            return this;
        }

        @Override
        public JDBCRatingDAO create() {
            if (cxnUrl == null)
                throw new UnsupportedOperationException("Cannot open session w/o URL");

            Connection dbc;
            try {
                if (properties == null) {
                    dbc = DriverManager.getConnection(cxnUrl);
                } else {
                    dbc = DriverManager.getConnection(cxnUrl, properties);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new JDBCRatingDAO(new JDBCDataSession(dbc, factory), true);
        }

        @Override
        public DataAccessObject snapshot() {
            DataAccessObject dao = create();
            if (takeSnapshot) {
                try {
                    List<Rating> ratings = Cursors.makeList(dao.getEvents(Rating.class));
                    return new EventCollectionDAO(ratings);
                } finally {
                    dao.close();
                }
            } else {
                return dao;
            }
        }

        /**
         * Wrap an existing database connection in a DAO.
         * @param cxn A database connection to use.
         * @return A DAO backed by the connection.
         */
        public JDBCRatingDAO open(Connection cxn) {
            return new JDBCRatingDAO(new JDBCDataSession(cxn, factory), false);
        }
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final JDBCDataSession session;
    protected final boolean ownsSession;

    public JDBCRatingDAO(JDBCDataSession session, boolean ownsSession) {
        this.session = session;
        this.ownsSession = ownsSession;
    }

    @Override
    public void close() {
        if (!ownsSession)
            return;

        try {
            session.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LongCursor getUsers() {
        try {
            PreparedStatement s = session.userStatement();
            return new IDCursor(s);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected int getCount(PreparedStatement s) throws SQLException {
        ResultSet rs = null;

        try {
            rs = s.executeQuery();
            if (!rs.next())
                throw new RuntimeException("User count query returned no rows");
            return rs.getInt(1);
        } finally {
            if (rs != null)
                rs.close();
        }
    }

    @Override
    public int getUserCount() {
        try {
            return getCount(session.userCountStatement());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LongCursor getItems() {
        try {
            PreparedStatement s = session.itemStatement();
            return new IDCursor(s);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        try {
            return getCount(session.itemCountStatement());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Cursor<Rating> getEvents() {
        return getEvents(SortOrder.ANY);
    }

    @Override
    public Cursor<Rating> getEvents(SortOrder order) {
        try {
            PreparedStatement s = session.eventStatement(order);
            return new RatingCursor(s);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E extends Event> Cursor<E> getEvents(Class<E> type, SortOrder order) {
        if (type.isAssignableFrom(Rating.class)) {
            return Cursors.transform(getEvents(order), MoreFunctions.cast(type));
        } else {
            return Cursors.empty();
        }
    }

    @Override
    public Cursor<Rating> getUserEvents(long userId) {
        try {
            PreparedStatement s = session.userEventStatement();
            s.setLong(1, userId);
            return new RatingCursor(s);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E extends Event> Cursor<E> getUserEvents(long uid, Class<E> type) {
        if (type.isAssignableFrom(Rating.class)) {
            return Cursors.transform(getUserEvents(uid), MoreFunctions.cast(type));
        } else {
            return Cursors.empty();
        }
    }

    @Override
    public Cursor<Rating> getItemEvents(long itemId) {
        try {
            PreparedStatement s = session.itemEventStatement();
            s.setLong(1, itemId);
            return new RatingCursor(s);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E extends Event> Cursor<E> getItemEvents(long iid, Class<E> type) {
        if (type.isAssignableFrom(Rating.class)) {
            return Cursors.transform(getItemEvents(iid), MoreFunctions.cast(type));
        } else {
            return Cursors.empty();
        }
    }

    static class IDCursor extends AbstractLongCursor {
        private Logger logger = LoggerFactory.getLogger(getClass());
        private ResultSet rset;
        private boolean advanced;
        private boolean valid;

        public IDCursor(PreparedStatement stmt) throws SQLException {
            advanced = false;
            rset = stmt.executeQuery();
        }

        @Override
        public boolean hasNext() {
            if (!advanced) {
                try {
                    valid = rset.next();
                } catch (SQLException e) {
                    logger.error("Error fetching row", e);
                }
                advanced = true;
            }
            return valid;
        }

        @Override
        public long nextLong() {
            if (!hasNext())
                throw new NoSuchElementException();
            advanced = false;
            try {
                return rset.getLong(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
            try {
                rset.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class RatingCursor extends AbstractRatingCursor<Rating> {
        private ResultSet resultSet;
        private boolean hasTimestampColumn;
        private MutableRating rating;

        public RatingCursor(PreparedStatement stmt) throws SQLException {
            rating = new MutableRating();
            resultSet = stmt.executeQuery();
            try {
                hasTimestampColumn = resultSet.getMetaData().getColumnCount() > 4;
            } catch (SQLException e) {
                resultSet.close();
                throw e;
            } catch (RuntimeException e) {
                resultSet.close();
                throw e;
            }
        }

        @Override
        public Rating poll() {
            try {
                if (!resultSet.next())
                    return null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try {
                rating.setId(resultSet.getLong(COL_EVENT_ID));
                if (resultSet.wasNull())
                    throw new RuntimeException("Unexpected null event ID");
                rating.setUserId(resultSet.getLong(COL_USER_ID));
                if (resultSet.wasNull())
                    throw new RuntimeException("Unexpected null user ID");
                rating.setItemId(resultSet.getLong(COL_ITEM_ID));
                if (resultSet.wasNull())
                    throw new RuntimeException("Unexpected null item ID");
                rating.setRating(resultSet.getDouble(COL_RATING));
                if (resultSet.wasNull())
                    rating.setRating(Double.NaN);
                long ts = -1;
                if (hasTimestampColumn) {
                    ts = resultSet.getLong(COL_TIMESTAMP);
                    if (resultSet.wasNull())
                        ts = -1;
                }
                rating.setTimestamp(ts);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return rating;
        }

        @Override
        public void close() {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
