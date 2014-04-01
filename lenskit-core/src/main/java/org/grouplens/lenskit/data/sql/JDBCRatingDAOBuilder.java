package org.grouplens.lenskit.data.sql;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import org.grouplens.lenskit.data.event.Rating;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;
import java.util.List;

/**
 * Construct and configure a JDBC rating DAO. Get a builder with {@link JDBCRatingDAO#newBuilder()}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class JDBCRatingDAOBuilder {
    private SQLStatementFactory factory;
    private BasicSQLStatementFactory basicFactory;
    private boolean closeWhenClosed;
    private CacheBuilder<? super QueryKey, ? super List<?>> cacheBuilder;
    private Cache<QueryKey, List<Rating>> queryCache;

    JDBCRatingDAOBuilder() {
        factory = basicFactory = new BasicSQLStatementFactory();
        cacheBuilder = CacheBuilder.newBuilder().softValues().maximumSize(1000);
    }

    public String getTableName() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getTableName();
    }

    public JDBCRatingDAOBuilder setTableName(String table) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        basicFactory.setTableName(table);
        return this;
    }

    public String getUserColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getUserColumn();
    }

    public JDBCRatingDAOBuilder setUserColumn(@Nonnull String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        basicFactory.setUserColumn(col);
        return this;
    }

    public String getItemColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getItemColumn();
    }

    public JDBCRatingDAOBuilder setItemColumn(@Nonnull String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        basicFactory.setItemColumn(col);
        return this;
    }

    public String getRatingColumn() {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
        return basicFactory.getRatingColumn();
    }

    public JDBCRatingDAOBuilder setRatingColumn(@Nonnull String col) {
        Preconditions.checkState(basicFactory != null, "statement factory has been explicitly set");
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
     * soft values and a maximum size of 1000.
     *
     * @param cb The cache builder.
     * @return The builder (for chaining).
     */
    public JDBCRatingDAOBuilder setCacheBuilder(CacheBuilder<? super QueryKey, ? super List<?>> cb) {
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
    public JDBCRatingDAOBuilder setCache(Cache<QueryKey,List<Rating>> cache) {
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
        Cache<QueryKey, List<Rating>> cache = queryCache;
        if (cache == null) {
            cache = cacheBuilder.build();
        }
        return new JDBCRatingDAO(con, factory, closeWhenClosed, cache);
    }
}
