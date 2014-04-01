package org.grouplens.lenskit.data.sql;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Connection;

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

    JDBCRatingDAOBuilder() {
        factory = basicFactory = new BasicSQLStatementFactory();
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
     * Create a DAO backed by a database connection.
     * @param con The database connection.
     * @return A DAO backed by {@code con}.
     */
    @SuppressWarnings("deprecation")
    public JDBCRatingDAO build(Connection con) {
        return new JDBCRatingDAO(con, factory, closeWhenClosed);
    }
}
