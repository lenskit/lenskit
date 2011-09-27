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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.sql.SQLStatementFactory;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;

/**
 * A train-test data set backed by a database.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class DBTTDataSet implements TTDataSet {
    private @Nullable String driver;
    private @Nonnull String connectionString;
    private String name;
    private SQLStatementFactory trainStatementFactory =
            new BasicSQLStatementFactory();
    private SQLStatementFactory testStatementFactory =
            new BasicSQLStatementFactory();
    
    public DBTTDataSet(@Nonnull String dsn) {
        connectionString = dsn;
    }
    
    @Override
    public long lastUpdated(PreparationContext context) {
        return 0L;
    }

    @Override
    public void prepare(PreparationContext context) throws PreparationException {
        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new PreparationException("Could not load DB driver", e);
            }
        }
    }

    /**
     * Set the name of this data set.
     * 
     * @param name The data set name.
     * @return The data set for chaining.
     */
    public DBTTDataSet setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the database driver class name. In the preparation phase, the data
     * set attempts to load this class to make sure the database driver is
     * registered.
     * 
     * @param drv The name of the database driver class.
     * @return The data set for chaining.
     */
    public DBTTDataSet setDriver(@Nullable String drv) {
        driver = drv;
        return this;
    }
    
    /**
     * @see #setDriver(String)
     */
    public @Nullable String getDriver() {
        return driver;
    }
    
    /**
     * Set the database connection string.
     * @param dsn The connection string.
     * @return The data set for chaining.
     */
    public DBTTDataSet setConnectionString(@Nonnull String dsn) {
        connectionString = dsn;
        return this;
    }
    
    /**
     * Get the database connection string.
     * @see #setConnectionString(String)
     */
    public @Nonnull String getConnectionString() {
        return connectionString;
    }

    /**
     * @return the trainStatementFactory
     */
    public SQLStatementFactory getTrainStatementFactory() {
        return trainStatementFactory;
    }

    /**
     * @param sf The statement factory for the training data.
     * @return This object for chaining.
     */
    public DBTTDataSet setTrainStatementFactory(SQLStatementFactory sf) {
        trainStatementFactory = sf;
        return this;
    }

    /**
     * @return the testStatementFactory
     */
    public SQLStatementFactory getTestStatementFactory() {
        return testStatementFactory;
    }

    /**
     * @param sf The statement factory for the test data.
     * @return This object for chaining
     */
    public DBTTDataSet setTestStatementFactory(SQLStatementFactory sf) {
        testStatementFactory = sf;
        return this;
    }

    @Override
    public void release() {
        /* Do nothing */
    }

    @Override
    public DAOFactory getTrainFactory() {
        return new JDBCRatingDAO.Factory(connectionString, trainStatementFactory);
    }

    @Override
    public DAOFactory getTestFactory() {
        return new JDBCRatingDAO.Factory(connectionString, testStatementFactory);
    }
    
    @Override
    public String toString() {
        return String.format("{DB %s}", connectionString);
    }
}
