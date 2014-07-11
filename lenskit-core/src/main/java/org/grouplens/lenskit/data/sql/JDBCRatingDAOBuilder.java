/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * Construct and configure a JDBC rating DAO. Get a builder with {@link JDBCRatingDAO#newBuilder()}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class JDBCRatingDAOBuilder {
    private SQLStatementFactory factory;
    private BasicSQLStatementFactory basicFactory;
    private boolean closeWhenClosed = true;
    private CacheBuilder<? super QueryKey, Object> cacheBuilder;
    private Cache<QueryKey, Object> queryCache;

    JDBCRatingDAOBuilder() {
        factory = basicFactory = new BasicSQLStatementFactory();
        cacheBuilder = CacheBuilder.newBuilder()
                                   .softValues()
                                   .maximumSize(1000)
                                   .expireAfterWrite(5, TimeUnit.MINUTES);
    }

    public String getTableName() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getTableName();
    }

    public JDBCRatingDAOBuilder setTableName(String table) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        Preconditions.checkNotNull(table, "table name");
        basicFactory.setTableName(table);
        return this;
    }

    public String getUserColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getUserColumn();
    }

    public JDBCRatingDAOBuilder setUserColumn(@Nonnull String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        Preconditions.checkNotNull(col, "user column name");
        basicFactory.setUserColumn(col);
        return this;
    }

    public String getItemColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getItemColumn();
    }

    public JDBCRatingDAOBuilder setItemColumn(@Nonnull String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        Preconditions.checkNotNull(col, "item column name");
        basicFactory.setItemColumn(col);
        return this;
    }

    public String getRatingColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getRatingColumn();
    }

    public JDBCRatingDAOBuilder setRatingColumn(@Nonnull String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        Preconditions.checkNotNull(col, "rating column name");
        basicFactory.setRatingColumn(col);
        return this;
    }

    public String getTimestampColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getTimestampColumn();
    }

    public JDBCRatingDAOBuilder setTimestampColumn(@Nullable String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        basicFactory.setTimestampColumn(col);
        return this;
    }

    public boolean isCloseWhenClosed() {
        return closeWhenClosed;
    }

    /**
     * Configure whether the the DAO should close the database connection.
     *
     * @param close {@code true} to close the database connection with the DAO, {@code false} to
     *              leave it open.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setCloseWhenClosed(boolean close) {
        this.closeWhenClosed = close;
        return this;
    }

    /**
     * Set the statement factory to be used by the DAO.
     * @param fac The statement factory.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setStatementFactory(SQLStatementFactory fac) {
        factory = fac;
        basicFactory = null;
        return this;
    }

    /**
     * Get the statement factory the DAO will use.
     * @return The statement factory the DAO will use.
     */
    public SQLStatementFactory getStatementFactory() {
        return factory;
    }

    /**
     * Set a cache builder to use for making the DAO's internal caches.  The default builder uses
     * soft value, a maximum size of 1000, and a timeout of 5 minutes after load.
     *
     * @param cb The cache builder.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setCacheBuilder(CacheBuilder<? super QueryKey, Object> cb) {
        cacheBuilder = cb;
        return this;
    }

    /**
     * Set a cache builder spec to use for making the DAO's internal caches.
     * @param spec A cache builder spec.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setCacheBuilder(CacheBuilderSpec spec) {
        return setCacheBuilder(CacheBuilder.from(spec));
    }

    /**
     * Set the cache builder spec to use for making the DAO's internal caches.
     * @param spec A cache builder spec.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setCacheBuilder(String spec) {
        return setCacheBuilder(CacheBuilderSpec.parse(spec));
    }

    /**
     * Set the cache to use for user and item queries.  Multiple instances can use the same cache safely.
     * This overides {@link #setCacheBuilder(com.google.common.cache.CacheBuilder)}.
     * @param cache The cache to use.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setCache(Cache<QueryKey,Object> cache) {
        queryCache = cache;
        return this;
    }

    /**
     * Create a DAO backed by a database connection.
     * @param con The database connection.
     * @return A DAO backed by {@code con}.
     */
    @SuppressWarnings("deprecation")
    public JDBCRatingDAO build(Connection con) {
        Cache<QueryKey, Object> cache = queryCache;
        if (cache == null) {
            cache = cacheBuilder.build();
        }
        return new JDBCRatingDAO(con, factory, closeWhenClosed, cache);
    }
}
