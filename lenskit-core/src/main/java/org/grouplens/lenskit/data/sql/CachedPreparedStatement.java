package org.grouplens.lenskit.data.sql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * A simple object that wraps a lazy initialized {@link PreparedStatement}.
 * @author Daniel Kluver <kluver@cs.umn.edu>
 *
 */
public class CachedPreparedStatement implements Callable<PreparedStatement>, Closeable{
    private PreparedStatement cache = null;
    private final String sql;
    private final Connection dbc;
    
    public CachedPreparedStatement(Connection dbc, String sql) {
        this.dbc = dbc;
        this.sql = sql;
    }
    
    @Override
    public PreparedStatement call() throws SQLException {
        if (cache == null) {
            cache = dbc.prepareStatement(sql);
        }
        return cache;
    }

    /**
     * When closed the underlying {@link PreparedStatement} will be closed. A new 
     * {@link PreparedStatement} will be made if {@link #call()} is called on the same object. 
     */
    @Override
    public void close() throws IOException {
        try {
            cache.close();
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            cache = null;
        }
    }

}
