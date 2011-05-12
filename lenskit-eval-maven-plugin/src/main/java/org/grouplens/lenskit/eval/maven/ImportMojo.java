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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.ScannerRatingCursor;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * @goal import
 */
public class ImportMojo extends AbstractDatabaseMojo {
    /**
     * The project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
    private MavenProject project;
    
    /**
     * The table in the database to load the data file.
     * 
     * @parameter expression="${lenskit.db.table}" default-value="ratings"
     * @required
     */
    private String table;

    /**
     * Source file to import into the database. If the file does not exist, it
     * is resolved on the classpath (so data sets can be stored in Maven JARs).
     * 
     * @parameter expression="${lenskit.import.source}"
     * @required
     */
    private String source;

    /**
     * Delimiter.
     * 
     * @parameter expression="${lenskit.delimiter}" default-value="\t"
     * @required
     */
    private String delimiter;

    /**
     * Whether to include a timestamp field.
     * 
     * @parameter expression="${lenskit.import.timestamp}" default-value="true"
     */
    private boolean useTimestamp;
    
    /**
     * Whether to drop the table before creating it. If this is <tt>false</tt>
     * and the table exists, the import will fail.
     * @parameter expression="${lenskit.import.drop}" default-value="false"
     */
    private boolean drop;

    private File stampFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (checkBuildState()) {
            getLog().info("Database " + getDatabaseConnectString() + " up to date.");
            return;
        }
        
        Cursor<Rating> inputRatings = null;
        Connection dbc = null;
        try {
            inputRatings = openSource();
            
            dbc = openDatabase();
            if (drop)
                dropDBTable(dbc);
            createDBTable(dbc);
            
            getLog().debug("Disabling autocommit for insert");
            dbc.setAutoCommit(false);
            getLog().info("Writing ratings to " + database);
            insertRatings(dbc, inputRatings);
            getLog().info("Committing transaction");
            dbc.commit();
            getLog().info("Creating indices");
            dbc.setAutoCommit(true);
            indexTable(dbc, "user");
            indexTable(dbc, "item");
            analyze(dbc);
            
            if (stampFile != null) {
                try {
                    FileUtils.fileWrite(stampFile.getPath(), "DBSTAMP");
                } catch (IOException e) {
                    throw new MojoExecutionException("Error updating stamp file", e);
                }
            }
        } catch (SQLException e) {
            throw new MojoExecutionException("SQL error", e);
        } finally {
            if (inputRatings != null)
                inputRatings.close();
            if (dbc != null) {
                try {
                    dbc.close();
                } catch (SQLException e) {
                    throw new MojoExecutionException("Error closing DB", e);
                }
            }
        }
    }

    /**
     * Check the status of the build, returning <tt>true</tt> if the database is
     * up to date.
     * 
     * @return
     * @throws MojoExecutionException
     */
    protected boolean checkBuildState() throws MojoExecutionException {
        if (databaseFile != null)
            return false;
        
        File dbFile = new File(databaseFile);
        dbFile.getParentFile().mkdirs();
        
        if (!dbFile.exists()) {
            getLog().debug("DB file does not exist, rebuilding");
            return false;
        }
        
        File sfile = new File(source);
        if (!sfile.exists() && getClass().getClassLoader().getResource(source) != null) {
            getLog().debug("Source file on classpath, not rebuilding");
            return true;
        }
        
        if (sfile.exists() && sfile.lastModified() <= dbFile.lastModified()) {
            getLog().debug("DB file newer than source file, not rebuilding");
            return true;
        }
        
        getLog().debug("Source file missing or DB out of date, proceeding with build");
        return false;
    }
    
    protected void dropDBTable(Connection dbc) throws SQLException {
        Statement stmt = dbc.createStatement();
        try {
            String query = String.format("DROP TABLE IF EXISTS %s;", table);
            getLog().debug("Executing: " + query);
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }
    
    protected void createDBTable(Connection dbc) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ");
        query.append(table);
        query.append(" (user INTEGER, item INTEGER, rating REAL");
        if (useTimestamp)
            query.append(", timestamp INTEGER");
        query.append(");");
        getLog().debug("Executing: " + query);
        Statement stmt = dbc.createStatement();
        try {
            stmt.execute(query.toString());
        } finally {
            stmt.close();
        }
    }
    
    protected void insertRatings(Connection dbc, Cursor<Rating> ratings) throws SQLException {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ");
        query.append(table);
        query.append(" (user, item, rating");
        if (useTimestamp)
            query.append(", timestamp");
        query.append(") VALUES (?, ?, ?");
        if (useTimestamp)
            query.append(", ?");
        query.append(");");
        
        getLog().debug("Executing: " + query);
        PreparedStatement stmt = dbc.prepareStatement(query.toString());
        boolean progress = getLog().isInfoEnabled();
        try {
            int n = 0;
            for (Rating r: ratings) {
                stmt.setLong(1, r.getUserId());
                stmt.setLong(2, r.getItemId());
                stmt.setDouble(3, r.getRating());
                if (useTimestamp) {
                    long ts = r.getTimestamp();
                    if (ts < 0)
                        stmt.setNull(4, Types.INTEGER);
                    else
                        stmt.setLong(4, ts);
                }
                stmt.executeUpdate();
                ++n;
                if (progress && n % 50 == 0)
                    System.out.format("%d\r", n);
            }
            getLog().info(String.format("Inserted %d ratings", n));
        } finally {
            stmt.close();
        }
    }

    protected Cursor<Rating> openSource() throws MojoFailureException {
        Scanner scanner;
        try {
            scanner = new Scanner(new File(source));
        } catch (FileNotFoundException e) {
            // resolve in classpath
            InputStream c = getClass().getClassLoader()
                    .getResourceAsStream(source);
            if (c == null)
                throw new MojoFailureException("Source file not found");
            scanner = new Scanner(c);
        }

        return new ScannerRatingCursor(scanner, source, delimiter);
    }
    
    protected void indexTable(Connection dbc, String column) throws SQLException {
        Statement stmt = dbc.createStatement();
        try {
            String query = String.format("CREATE INDEX %s_%s_idx ON %s (%s);",
                                         table, column, table, column);
            getLog().debug("Executing: " + query);
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }
    
    protected void analyze(Connection dbc) throws SQLException {
        Statement stmt = dbc.createStatement();
        try {
            String query = String.format("ANALYZE %s;", table);
            getLog().debug("Executing: " + query);
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }
}
