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
package org.grouplens.lenskit.eval.data.traintest;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.JDBCUtils;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.LockingMode;

import com.google.common.io.Files;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CrossfoldTTDataSet implements TTDataSet {
    private Logger logger = LoggerFactory.getLogger(CrossfoldTTDataSet.class);
    
    private CrossfoldManager manager;
    private final int foldNumber;
    private final String stampName;
    private final String dbName;
    private File dbFile;
    private boolean useTimestamp = true;
    private DBTTDataSet dataset;

    public CrossfoldTTDataSet(CrossfoldManager mgr, int fold) {
	    manager = mgr;
	    foldNumber = fold;
	    stampName = String.format("data.%d.stamp", fold);
	    dbName = String.format("data.%d.db", fold);
    }
    
    protected String getDSN() {
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

	@Override
	public long lastUpdated(PreparationContext context) {
	    File stamp = new File(manager.cacheDir(context), stampName);
	    return stamp.exists() ? stamp.lastModified() : -1L;
	}

    @Override
    public void prepare(PreparationContext context) throws PreparationException {
        context.prepare(manager);
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e1) {
            throw new PreparationException("Cannot load JDBC driver", e1);
        }
        
        dbFile = new File(manager.cacheDir(context), dbName);
        
        if (context.isUnconditional() || lastUpdated(context) < manager.lastUpdated(context)) {
            importDataSet(context);
        } else {
            logger.debug("Data set {} up to date", this);
        }
        
        BasicSQLStatementFactory trainSF = new BasicSQLStatementFactory();
        trainSF.setTableName("train");
        trainSF.setTimestampColumn(useTimestamp ? "timestamp" : null);
        BasicSQLStatementFactory testSF = new BasicSQLStatementFactory();
        testSF.setTableName("test");
        testSF.setTimestampColumn(useTimestamp ? "timestamp" : null);
        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        //config.setLockingMode(LockingMode.NORMAL);
        dataset = new DBTTDataSet(getDSN())
                .setTrainStatementFactory(trainSF)
                .setTestStatementFactory(testSF)
                .setProperties(config.toProperties());
        
        context.prepare(dataset);
    }

    /**
     * @param context
     * @throws PreparationException
     */
    void importDataSet(PreparationContext context) throws PreparationException {
        logger.debug("Importing fold {} for {}",
                     foldNumber, manager.getSource().getName());
        dbFile.delete();
        
        DataAccessObject dao = manager.getSource().getDAOFactory().create();
        try {
            Connection dbc = DriverManager.getConnection(getDSN());
            try {
                writePartitionData(context, dao, dbc);
            } finally {
                dbc.close();
            }
        } catch (SQLException e) {
            throw new PreparationException("Failed to write database", e);
        } finally {
            dao.close();
        }
        
        try {
            Files.touch(new File(manager.cacheDir(context), stampName));
        } catch (IOException e) {
            throw new PreparationException("Failed to update stamp", e); 
        }
    }
    
    private String makeInsertSQL(String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(table);
        sb.append(" (id, user, item, rating");
        if (useTimestamp)
            sb.append(", timestamp");
        sb.append(") VALUES (?, ?, ?, ?");
        if (useTimestamp)
            sb.append(", ?");
        sb.append(")");
        logger.debug("Insert SQL: {}", sb);
        return sb.toString();
    }
    
    private String makeCreateSQL(String table) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(table);
        sb.append(" (id INTEGER PRIMARY KEY, user INTEGER NOT NULl, item INTEGER NOT NULL, rating REAL");
        if (useTimestamp)
            sb.append(", timestamp INTEGER NULL");
        sb.append(")");
        return sb.toString();
    }
    
    private Long2ObjectMap<List<Rating>> initUserMap(PreparationContext context) {
        LongList users = manager.getFoldUsers(context, foldNumber);
        Long2ObjectOpenHashMap<List<Rating>> map =
                new Long2ObjectOpenHashMap<List<Rating>>(users.size());
        LongIterator iter = users.iterator();
        while (iter.hasNext()) {
            map.put(iter.nextLong(), new ArrayList<Rating>());
        }
        return map;
    }
    
    private void bindRating(PreparedStatement stmt, Rating r) throws SQLException {
        stmt.setLong(1, r.getId());
        stmt.setLong(2, r.getUserId());
        stmt.setLong(3, r.getItemId());
        Preference p = r.getPreference();
        if (p == null) {
            stmt.setNull(4, Types.REAL);
        } else {
            stmt.setDouble(4, p.getValue());
        }
        if (useTimestamp) {
            long ts = r.getTimestamp();
            if (ts >= 0) {
                stmt.setLong(5, ts);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
        }
    }
    
    private void writePartitionData(PreparationContext context, 
                                    DataAccessObject dao, Connection dbc) throws SQLException {
        HoldoutMode mode = manager.getHoldoutMode();
        int n = manager.getHoldoutCount();
        PreparedStatement insTrain = null, insTest = null;
        
        dbc.setAutoCommit(false);
        
        try {
            JDBCUtils.execute(dbc, makeCreateSQL("train"));
            JDBCUtils.execute(dbc, makeCreateSQL("test"));
            insTrain = dbc.prepareStatement(makeInsertSQL("train"));
            insTest = dbc.prepareStatement(makeInsertSQL("test"));
            
            Long2ObjectMap<List<Rating>> userEvents = initUserMap(context);
            Cursor<Rating> ratings = dao.getEvents(Rating.class);
            try {
                logger.debug("Writing training data");
                for (Rating r: ratings.fast()) {
                    long uid = r.getUserId();
                    if (userEvents.containsKey(uid)) {
                        userEvents.get(uid).add(r.clone());
                    } else {
                        bindRating(insTrain, r);
                        insTrain.execute();
                    }
                }
            } finally {
                ratings.close();
            }
            
            logger.debug("Splitting and writing query users");
            for (List<Rating> history: userEvents.values()) {
                int idx = mode.partition(history, n);
                ListIterator<Rating> iter = history.listIterator();
                while (iter.hasNext()) {
                    int i = iter.nextIndex();
                    Rating r = iter.next();
                    PreparedStatement s = (i < idx) ? insTrain : insTest;
                    bindRating(s, r);
                    s.execute();
                }
            }
        } finally {
            JDBCUtils.close(insTrain, insTest);
        }
        
        logger.debug("Committing data");
        dbc.commit();
        dbc.setAutoCommit(true);
        
        logger.debug("Indexing tables");
        JDBCUtils.execute(dbc, "CREATE INDEX train_user_idx ON train (user);");
        JDBCUtils.execute(dbc, "CREATE INDEX train_item_idx ON train (item);");
        if (useTimestamp)
            JDBCUtils.execute(dbc, "CREATE INDEX train_timestamp_idx ON train (timestamp);");
        JDBCUtils.execute(dbc, "CREATE INDEX test_user_idx ON test (user);");
        JDBCUtils.execute(dbc, "CREATE INDEX test_item_idx ON test (item);");
        JDBCUtils.execute(dbc, "ANALYZE;");
    }

	@Override
    public String getName() {
	    return String.format("%s:%d", manager.getSource().getName(), foldNumber);
    }

	@Override
    public void release() {
	    dbFile = null;
    }

	@Override
    public DAOFactory getTrainFactory() {
	    if (dataset == null)
	        throw new IllegalStateException("Data set not prepared");
	    return dataset.getTrainFactory();
    }

	@Override
    public DAOFactory getTestFactory() {
	    if (dataset == null)
	        throw new IllegalStateException("Data set not prepared");
	    return dataset.getTestFactory();
    }
	
	@Override
	public String toString() {
	    return String.format("{Crossfold %s:%d}", manager.getSource(), foldNumber);
	}
}
