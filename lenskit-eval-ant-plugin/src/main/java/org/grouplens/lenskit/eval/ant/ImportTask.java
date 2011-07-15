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

import static org.grouplens.lenskit.data.sql.JDBCRatingDAO.COL_EVENT_ID;
import static org.grouplens.lenskit.data.sql.JDBCRatingDAO.COL_ITEM_ID;
import static org.grouplens.lenskit.data.sql.JDBCRatingDAO.COL_RATING;
import static org.grouplens.lenskit.data.sql.JDBCRatingDAO.COL_TIMESTAMP;
import static org.grouplens.lenskit.data.sql.JDBCRatingDAO.COL_USER_ID;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.dao.ScannerRatingCursor;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.sql.JDBCUtils;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ImportTask extends Task {
	private File sourceFile;
	private File dbFile;
	private String tableName;
	private String delimiter = "\t";
	private boolean useTimestamp = true;
	
	public void setSource(File file) {
		sourceFile = file;
	}
	public void setDatabase(File file) {
		dbFile = file;
	}
	public void setDelimiter(String d) {
		delimiter = d;
	}
	public void setTable(String table) {
	    tableName = table;
	}
	public void setTimestamp(boolean useTimestamp) {
		this.useTimestamp = useTimestamp;
	}
	
	@Override
	public void execute() throws BuildException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new BuildException("Could not find SQLite JDBC driver", e);
        }
        // TODO Support importing data from the classpath
        Cursor<Rating> ratings;
        try {
            log("Reading ratings from " + sourceFile);
            Scanner s = new Scanner(sourceFile);
            ratings = new ScannerRatingCursor(s, sourceFile.getPath(), delimiter);
        } catch (FileNotFoundException e) {
            throw new BuildException("Cannot open data file " + sourceFile.getPath(), e);
        }
        try {
            writeRatings(ratings);
        } catch (SQLException e) {
            throw new BuildException("Error splitting ratings", e);
        } finally {
            ratings.close();
        }
    }
    
    protected void writeRatings(Cursor<Rating> ratings) throws SQLException, BuildException {
        Connection dbc = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
        try {
            // create tables
            JDBCUtils.execute(dbc,
                              String.format("DROP TABLE IF EXISTS %s;", tableName));
            String qmake = "CREATE TABLE %s (id INTEGER, user INTEGER, item INTEGER, rating REAL";
            if (useTimestamp)
                qmake += ", timestamp INTEGER";
            qmake += ");";
            JDBCUtils.execute(dbc, String.format(qmake, tableName));
            dbc.setAutoCommit(false);
            
            qmake = "INSERT INTO %s (id, user, item, rating";
            if (useTimestamp) qmake += ", timestamp";
            qmake += ") VALUES (?, ?, ?, ?";
            if (useTimestamp) qmake += ", ?";
            qmake += ");";
            PreparedStatement insert = dbc.prepareStatement(String.format(qmake, tableName));

            try {
                log("Writng ratings to " + dbFile.getPath());
                for (Rating r: ratings) {
                    bindRating(insert, r, useTimestamp);
                    insert.executeUpdate();
                }
            } finally {
                insert.close();
            }

            log("Committing data");
            dbc.commit();
            dbc.setAutoCommit(true);
            JDBCUtils.execute(dbc,
                String.format("CREATE INDEX %s_user_idx ON %s (user);",
                    tableName, tableName));
            JDBCUtils.execute(dbc,
                String.format("CREATE INDEX %s_item_idx ON %s (item);",
                    tableName, tableName));
            if (useTimestamp) {
                JDBCUtils.execute(dbc,
                    String.format("CREATE INDEX %s_timestamp_idx ON %s (timestamp);",
                        tableName, tableName));
            }
            JDBCUtils.execute(dbc, "ANALYZE;");
        } finally {
            dbc.close();
        }
	}
    /**
     * Fill in the values in a prepared statement from a rating object. Expects
     * the statement to have 4 or 5 parameters:
     * (id,user,item,rating,timestamp?). Uses the column definitions in
     * {@link JDBCRatingDAO}.
     * 
     * @param stmt The prepared statement.
     * @param r The rating value.
     * @param useTimestamp If <tt>true</tt>, insert the timestamp as the 5th
     *            parameter.
     * @throws SQLException
     */
    static void bindRating(PreparedStatement stmt, Rating r, boolean useTimestamp) throws SQLException {
        stmt.setLong(COL_EVENT_ID, r.getId());
        stmt.setLong(COL_USER_ID, r.getUserId());
        stmt.setLong(COL_ITEM_ID, r.getItemId());
        
        Preference p = r.getPreference();
        if (p != null) {
            stmt.setDouble(COL_RATING, p.getValue());
        } else {
            stmt.setNull(COL_RATING, Types.DOUBLE);
        }
        
        if (useTimestamp) {
            long ts = r.getTimestamp();
            if (ts >= 0)
                stmt.setLong(COL_TIMESTAMP, ts);
            else
                stmt.setNull(COL_TIMESTAMP, Types.INTEGER);
        }
    }
}
