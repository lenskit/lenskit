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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractDatabaseMojo extends AbstractMojo {

    /**
     * The class to load to make the JDBC driver available.
     * @parameter expression="${lenskit.datatabaseDriver}"
     */
    protected String databaseDriver;
    /**
     * The URL for the database connection (passed to {@link DriverManager}). If
     * unspecified, {@link #databaseFile} is opened as an SQLite database. If
     * specified, {@link #databaseFile} is interpreted as a stamp file.
     * 
     * <p>Don't use this unless you really need to; prefer to use {@link #databaseFile}
     * and SQLite.
     * 
     * @parameter expression="${lenskit.database}"
     */
    protected String database;
    /**
     * The file that the database will be stored in (used to check modification
     * times). Opened as an SQLite database if {@link #database} is not
     * specified.
     * 
     * @parameter expression="${lenskit.databaseFile}"
     */
    protected String databaseFile;
    
    protected String getDatabaseConnectString() {
        if (database == null)
            return "jdbc:sqlite:" + databaseFile;
        else
            return database;
    }
    
    protected Connection openDatabase() throws SQLException, MojoExecutionException {
        try {
            if (databaseDriver != null)
                Class.forName(databaseDriver);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Cannot find database driver", e);
        }
        return DriverManager.getConnection(getDatabaseConnectString());
    }

}