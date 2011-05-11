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
package org.grouplens.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.sql.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a train/test ratings set. It takes care of partitioning the
 * data set into N portions so that each one can be tested against the others.
 * Portions are divided equally, and data is randomized before being
 * partitioned.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CrossfoldManager {
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldManager.class);
    private final int chunkCount;
    private Connection connection;

    /**
     * Construct a new train/test ratings set.
     * @param nfolds The number of portions to divide the data set into.
     * @param ratings The ratings data to partition.
     */
    public CrossfoldManager(int nfolds, String database, String table,
            UserRatingProfileSplitter splitter) {
        logger.debug("Creating rating set with {} folds", nfolds);
        
        try {
            connection = DriverManager.getConnection(database);
            partitionUsers(connection, table, nfolds, splitter);
        } catch (Exception e) {
            /* close if we're failing */
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e2) {
                    logger.error("Second error closing DB while handling error", e);
                    throw new RuntimeException("Error closing DB while failing", e2);
                }
            }
            throw new RuntimeException("Error setting up crossfold", e);
        }
        chunkCount = nfolds;
    }
    
    private static void partitionUsers(Connection cxn, String table, int nfolds,
                                        UserRatingProfileSplitter splitter) throws SQLException {
        Random rnd = new Random();
        Cursor<UserRatingProfile> userCursor = null;
        JDBCRatingDAO dao = null;
        try {
            BasicSQLStatementFactory sfac = new BasicSQLStatementFactory();
            sfac.setTableName(table);
            cxn.setAutoCommit(false);
            
            logger.debug("Creating crossfold table");
            
            JDBCUtils.execute(cxn, "CREATE TABLE crossfold_probe_set (" +
                "user INTEGER, item INTEGER, segment INTEGER);");
            
            int nusers = 0;
            String iq = "INSERT INTO crossfold_probe_set (user, item, segment) VALUES (?, ?, ?);";
            logger.debug("Preparing: {}", iq);
            PreparedStatement insert = cxn.prepareStatement(iq);
            logger.debug("Populating crossfold table");
            try {
                dao = new JDBCRatingDAO(null, sfac);
                dao.openSession(cxn);
                userCursor = dao.getUserRatingProfiles();
                for (UserRatingProfile user: userCursor) {
                    insert.setLong(1, user.getUser());
                    int n = rnd.nextInt(nfolds);
                    insert.setInt(3, n);
                    SplitUserRatingProfile sp = splitter.splitProfile(user);
                    for (Long2DoubleMap.Entry e: sp.getProbeVector()) {
                        insert.setLong(2, e.getLongKey());
                        insert.executeUpdate();
                    }
                    nusers++;
                }
            } finally {
                insert.close();
            }
            cxn.commit();
            cxn.setAutoCommit(true);
            logger.debug("Indexing crossfold table");
            JDBCUtils.execute(cxn, "CREATE INDEX crossfold_user_idx ON crossfold_probe_set (user)");
            JDBCUtils.execute(cxn, "CREATE INDEX crossfold_item_idx ON crossfold_probe_set (item)");
            JDBCUtils.execute(cxn, "CREATE INDEX crossfold_segment_idx ON crossfold_probe_set (segment);");
            JDBCUtils.execute(cxn, "ANALYZE;");
            
            logger.debug("Setting up views for indexing");
            for (int i = 0; i < nfolds; i++) {
                String q = "CREATE VIEW crossfold_train_" + i + " AS"
                    + " SELECT r.user AS user, r.item AS item, r.rating AS rating, r.timestamp AS timestamp"
                    + " FROM " + table + " r LEFT JOIN crossfold_probe_set p USING (user, item)"
                    + " WHERE p.segment IS NULL OR p.segment <> " + i;
                JDBCUtils.execute(cxn, q);
                q = "CREATE VIEW crossfold_test_" + i + " AS"
                    + " SELECT r.user, r.item, r.rating"
                    + " FROM " + table + " r, crossfold_probe_set p"
                    + " WHERE r.user = p.user AND r.item = p.item AND p.segment = " + i;
                JDBCUtils.execute(cxn, q);
            }
            
            logger.info("Partitioned {} users into {} folds", nusers, nfolds);
        } catch (SQLException e) {
            if (!cxn.getAutoCommit())
                cxn.rollback();
            throw e;
        } catch (RuntimeException e) {
            if (!cxn.getAutoCommit())
                cxn.rollback();
            throw e;
        } finally {
            if (userCursor != null)
                userCursor.close();
            if (dao != null)
                dao.closeSession();
            cxn.setAutoCommit(true);
        }    
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public void close() {
        try {
            if (connection != null)
                connection.close();
            connection = null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getChunkCount() {
        return chunkCount;
    }

    /**
     * Build a training data collection.  The collection is built on-demand, so
     * it doesn't use much excess memory.
     * @param testIndex The index of the test set to use.
     * @return The union of all data partitions except testIndex.
     */
    public String trainingSet(final int testIndex) {
        return String.format("crossfold_train_%d", testIndex);
    }

    /**
     * Return a test data set.
     * @param testIndex The index of the test set to use.
     * @return The test set of users.
     *
     * @todo Fix this method to be more efficient - currently, we convert from
     * vectors to ratings to later be converted back to vectors. That's slow.
     */
    public String testSet(final int testIndex) {
        return String.format("crossfold_test_%d", testIndex);
    }
}
