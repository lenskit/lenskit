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
/**
 * 
 */
package org.grouplens.lenskit.eval.ant;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.Ratings;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.dao.SimpleFileDAO;
import org.grouplens.lenskit.data.sql.JDBCUtils;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CrossfoldSplitTask extends Task {
	private String dataFile;
	private String dbFilePattern;
	private String delimiter = "\t";
	private int numFolds = 5;
	private boolean useTimestamp = true;
	private boolean timeSplit = false;
	private int holdoutCount = 10;
	
	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}
	public void setDbFile(String dbFilePattern) {
		this.dbFilePattern = dbFilePattern;
	}
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	public void setNumFolds(int numFolds) {
		this.numFolds = numFolds;
	}
	public void setUseTimestamp(boolean useTimestamp) {
		this.useTimestamp = useTimestamp;
	}
	public void setHoldoutCount(int holdoutCount) {
		this.holdoutCount = holdoutCount;
	}
	
	public void setMode(String mode) {
	    if (mode.toLowerCase().equals("time"))
	        timeSplit = true;
	    else if (mode.toLowerCase().equals("random"))
	        timeSplit = false;
	    else
	        throw new IllegalArgumentException("Invalid mode " + mode);
	}
	
	@Override
	public void execute() throws BuildException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new BuildException("Could not find SQLite JDBC driver", e);
        }
        // TODO Support importing data from the classpath
        SimpleFileDAO.Manager daoManager;
        try {
            log("Reading ratings from " + dataFile);
            daoManager = new SimpleFileDAO.Manager(new File(dataFile), delimiter);
        } catch (FileNotFoundException e) {
            throw new BuildException("Cannot open data file", e);
        }
        
        RatingDataAccessObject dao = daoManager.open();
        try {
            Long2IntMap userSegments = splitUsers(dao);
            writeRatings(dao, userSegments);
        } catch (SQLException e) {
            throw new BuildException("Error splitting ratings", e);
        } finally {
            dao.close();
        }
    }
    
    protected Long2IntMap splitUsers(RatingDataAccessObject dao) {
        Random r = new Random();
        Long2IntMap userMap = new Long2IntOpenHashMap();
        LongCursor users = dao.getUsers();
        try {
            while (users.hasNext()) {
                long uid = users.nextLong();
                userMap.put(uid, r.nextInt(numFolds));
            }
        } finally {
            users.close();
        }
        log(String.format("Partitioned %d users", userMap.size()),
        		Project.MSG_DEBUG);
        return userMap;
    }
    
    protected void writeRatings(RatingDataAccessObject dao, Long2IntMap userSegments) throws BuildException, SQLException {
        Connection[] dbcs = new Connection[numFolds];
        PreparedStatement[] insert = new PreparedStatement[numFolds];
        PreparedStatement[] test = new PreparedStatement[numFolds];
        try {
            // create tables
            for (int i = 0; i < numFolds; i++) {
                String fn = String.format(dbFilePattern, i+1);
                log("Opening database " + fn, Project.MSG_DEBUG);
                dbcs[i] = DriverManager.getConnection("jdbc:sqlite:" + fn);
                JDBCUtils.execute(dbcs[i], "DROP TABLE IF EXISTS train;");
                JDBCUtils.execute(dbcs[i], "DROP TABLE IF EXISTS test;");
                String qmake = "CREATE TABLE %s (user INTEGER, item INTEGER, rating REAL";
                if (useTimestamp)
                    qmake += ", timestamp INTEGER";
                qmake += ");";
                JDBCUtils.execute(dbcs[i], String.format(qmake, "train"));
                JDBCUtils.execute(dbcs[i], String.format(qmake, "test"));
                qmake = "INSERT INTO %s (user, item, rating";
                if (useTimestamp) qmake += ", timestamp";
                qmake += ") VALUES (?, ?, ?";
                if (useTimestamp) qmake += ", ?";
                qmake += ");";
                insert[i] = dbcs[i].prepareStatement(String.format(qmake, "train"));
                test[i] = dbcs[i].prepareStatement(String.format(qmake, "test"));
                dbcs[i].setAutoCommit(false);
            }
            
            // prepare maps for user ratings
            Long2ObjectMap<List<Rating>> userRatings = new Long2ObjectOpenHashMap<List<Rating>>(userSegments.size());
            LongIterator iter = userSegments.keySet().iterator();
            while (iter.hasNext())
                userRatings.put(iter.nextLong(), new ArrayList<Rating>());
            
            // take pass through ratings, collecting by user and adding to
            // training sets
            log("Processing ratings");
            Cursor<Rating> ratings = dao.getRatings();
            try {
                for (Rating r: ratings) {
                    long uid = r.getUserId();
                    int s = userSegments.get(uid);
                    userRatings.get(uid).add(r);
                    long ts = r.getTimestamp();
                    for (int i = 0; i < numFolds; i++) {
                        if (i != s) {
                            insert[i].setLong(1, uid);
                            insert[i].setLong(2, r.getItemId());
                            insert[i].setDouble(3, r.getRating());
                            if (useTimestamp) {
                                if (ts >= 0)
                                    insert[i].setLong(4, ts);
                                else
                                    insert[i].setNull(4, Types.INTEGER);
                            }
                            insert[i].executeUpdate();
                        }
                    }
                }
            } finally {
                ratings.close();
            }
            
            // run through the user's ratings, adding to train and test sets
            // as appropriate
            log("Writing test sets");
            for (Long2ObjectMap.Entry<List<Rating>> e: userRatings.long2ObjectEntrySet()) {
                long uid = e.getLongKey();
                int seg = userSegments.get(uid);
                PreparedStatement sTrain = insert[seg];
                PreparedStatement sTest = test[seg];
                sTrain.setLong(1, uid);
                sTest.setLong(1, uid);
                List<Rating> urs = e.getValue();
                if (timeSplit)
                    Collections.sort(urs, Ratings.TIMESTAMP_COMPARATOR);
                else
                    Collections.shuffle(urs);
                int midpt = urs.size() - holdoutCount;
                
                // Insert training data
                for (Rating r: urs.subList(0, midpt)) {
                    long iid = r.getItemId();
                    double v = r.getRating();
                    long ts = r.getTimestamp();
                    sTrain.setLong(2, iid);
                    sTrain.setDouble(3, v);
                    if (useTimestamp) {
                        if (ts >= 0)
                            sTrain.setLong(4, ts);
                        else
                            sTrain.setNull(4, Types.INTEGER);
                    }
                    sTrain.executeUpdate();
                }
                
                // Insert test data
                for (Rating r: urs.subList(midpt, urs.size())) {
                    long iid = r.getItemId();
                    double v = r.getRating();
                    long ts = r.getTimestamp();
                    sTest.setLong(2, iid);
                    sTest.setDouble(3, v);
                    if (useTimestamp) {
                        if (ts >= 0)
                            sTest.setLong(4, ts);
                        else
                            sTest.setNull(4, Types.INTEGER);
                    }
                    sTest.executeUpdate();
                }
                
                // done with ratings
                urs.clear();
            }
            
            userRatings = null;
            
            log("Committing data");
            // Commit and index
            for (int i = 0; i < numFolds; i++) {
                log(String.format("Committing and indexing set %d", i+1));
                dbcs[i].commit();
                dbcs[i].setAutoCommit(true);
                JDBCUtils.execute(dbcs[i], "CREATE INDEX train_user_idx ON train (user);");
                JDBCUtils.execute(dbcs[i], "CREATE INDEX train_item_idx ON train (item);");
                if (useTimestamp)
                    JDBCUtils.execute(dbcs[i], "CREATE INDEX train_timestamp_idx ON train (timestamp);");
                JDBCUtils.execute(dbcs[i], "CREATE INDEX test_user_idx ON test (user);");
                JDBCUtils.execute(dbcs[i], "CREATE INDEX test_item_idx ON test (item);");
                JDBCUtils.execute(dbcs[i], "ANALYZE;");
            }
        } finally {
            boolean failed = false;
            for (int i = 0; i < dbcs.length; i++) {
                if (test[i] != null) {
                    try {
                        test[i].close();
                    } catch (SQLException e) {
                        handleErrorOutput("Error closing: " + e.getMessage());
                        failed = true;
                    }
                }
                if (insert[i] != null) {
                    try {
                        insert[i].close();
                    } catch (SQLException e) {
                    	handleErrorOutput("Error closing: " + e.getMessage());
                        failed = true;
                    }
                }
                if (dbcs[i] != null) {
                    try {
                        dbcs[i].close();
                    } catch (SQLException e) {
                    	handleErrorOutput("Error closing: " + e.getMessage());
                        failed = true;
                    }
                }
                if (failed)
                    throw new BuildException("Failed to close database");
            }
        }
	}
}
