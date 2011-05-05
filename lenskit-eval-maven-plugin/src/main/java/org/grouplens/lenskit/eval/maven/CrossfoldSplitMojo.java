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
package org.grouplens.lenskit.eval.maven;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.slf4j.maven.MavenLoggerFactory;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.dao.SimpleFileDAO;
import org.grouplens.lenskit.data.sql.JDBCUtils;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @goal crossfold-split
 */
public class CrossfoldSplitMojo extends AbstractMojo {
    /**
     * The file containing rating data to split.
     * @parameter expression="${lenskit.dataFile}"
     * @required
     */
    private String dataFile;
    
    /**
     * Format string for generating database name patterns. It is expected to
     * have one "%d" which will be replaced by the fold number.
     * @parameter expression="${lenskit.databaseFilePattern}" 
     * @required
     */
    private String databaseFilePattern;
    
    /**
     * Stamp file for checking whether the DB is up to date.
     * @parameter expression="${lenskit.stampFile}"
     */
    private String stampFile;
    
    /**
     * Force rebuild even if stamp file is newer than data file.
     * @parameter expression="${lenskit.force}" default-value="false"
     */
    private boolean force;
    
    /**
     * The delimiter for parsing the input file.
     * @parameter expression="${lenskit.delimiter}"
     * @required
     */
    private String delimiter = "\t";
    
    /**
     * The number of folds to split the data into.
     * @parameter expression="${lenskit.numFulds}"
     */
    private int numFolds = 5;
    
    /**
     * Create a timestamp column in the output tables.
     * @parameter expression="${lenskit.useTimestamp}" default-value="true"
     */
    private boolean useTimestamp;
    
    /**
     * Number of items to hold out.
     * @parameter expression="${lenskit.holdout.count}" default-value="10"
     */
    private int holdoutCount;

    // TODO Use the Maven shim bridge
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isUpToDate()) {
            getLog().info("Database is already up to date (-Dlenskit.force=true to force");
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not find SQLite JDBC driver", e);
        }
        MavenLoggerFactory.setLog(getLog());
        new File(databaseFilePattern).getParentFile().mkdirs();
        // TODO Support importing data from the classpath
        RatingDataAccessObject dao;
        try {
            getLog().info("Reading ratings from " + dataFile);
            dao = new SimpleFileDAO(new File(dataFile), delimiter);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("Cannot open data file", e);
        }
        dao.openSession();
        try {
            Long2IntMap userSegments = splitUsers(dao);
            writeRatings(dao, userSegments);
        } catch (SQLException e) {
            throw new MojoFailureException("Error splitting ratings", e);
        } finally {
            dao.closeSession();
        }
        
        try {
            touch();
        } catch (IOException e) {
            throw new MojoExecutionException("Error touching stamp file", e);
        }
    }
    
    protected boolean isUpToDate() {
        if (stampFile == null)
            return false;
        
        if (force)
            return false;
        
        File source = new File(dataFile);
        File dest = new File(stampFile);
        
        if (!source.exists())
            return false;
        
        if (!dest.exists())
            return false;
        
        if (dest.lastModified() < source.lastModified())
            return false;
        
        return true;
    }
    
    protected void touch() throws IOException {
        if (stampFile != null) {
            FileUtils.fileWrite(stampFile, String.format("Imported %s\n", dataFile));
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
        getLog().debug(String.format("Partitioned %d users", userMap.size()));
        return userMap;
    }
    
    protected void writeRatings(RatingDataAccessObject dao, Long2IntMap userSegments) throws MojoExecutionException, SQLException {
        Connection[] dbcs = new Connection[numFolds];
        PreparedStatement[] insert = new PreparedStatement[numFolds];
        PreparedStatement[] test = new PreparedStatement[numFolds];
        try {
            // create tables
            for (int i = 0; i < numFolds; i++) {
                String fn = String.format(databaseFilePattern, i+1);
                getLog().debug("Opening database " + fn);
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
            getLog().info("Processing ratings");
            Cursor<Rating> ratings = dao.getRatings();
            try {
                int n = 0;
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
                    n++;
                    if (n % 50 == 0 && getLog().isInfoEnabled())
                        System.out.format("%d\r", n);
                }
                if (getLog().isInfoEnabled())
                    System.out.format("%d\n", n);
            } finally {
                ratings.close();
            }
            
            // run through the user's ratings, adding to train and test sets
            // as appropriate
            getLog().info("Writing test sets");
            int n = 0;
            for (Long2ObjectMap.Entry<List<Rating>> e: userRatings.long2ObjectEntrySet()) {
                long uid = e.getLongKey();
                int seg = userSegments.get(uid);
                PreparedStatement sTrain = insert[seg];
                PreparedStatement sTest = test[seg];
                sTrain.setLong(1, uid);
                sTest.setLong(1, uid);
                List<Rating> urs = e.getValue();
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
                for (Rating r: urs.subList(0, midpt)) {
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
                
                n++;
                if (n % 50 == 0 && getLog().isInfoEnabled())
                    System.out.format("%d\r", n);
            }
            if (getLog().isInfoEnabled())
                System.out.format("%d\n", n);
            
            userRatings = null;
            
            getLog().info("Committing data");
            // Commit and index
            for (int i = 0; i < numFolds; i++) {
                getLog().debug(String.format("Committing and indexing set %d", i+1));
                dbcs[i].commit();
                dbcs[i].setAutoCommit(true);
                JDBCUtils.execute(dbcs[i], "CREATE INDEX train_user_idx ON train (user);");
                JDBCUtils.execute(dbcs[i], "CREATE INDEX train_item_idx ON train (item);");
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
                        getLog().error(e);
                        failed = true;
                    }
                }
                if (insert[i] != null) {
                    try {
                        insert[i].close();
                    } catch (SQLException e) {
                        getLog().error(e);
                        failed = true;
                    }
                }
                if (dbcs[i] != null) {
                    try {
                        dbcs[i].close();
                    } catch (SQLException e) {
                        getLog().error(e);
                        failed = true;
                    }
                }
                if (failed)
                    throw new MojoExecutionException("Failed to close database");
            }
        }
    }
}
