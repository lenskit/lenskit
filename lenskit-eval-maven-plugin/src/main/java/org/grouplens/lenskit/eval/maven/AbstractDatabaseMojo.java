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