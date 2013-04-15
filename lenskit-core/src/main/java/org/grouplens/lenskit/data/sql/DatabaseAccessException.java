package org.grouplens.lenskit.data.sql;

import org.grouplens.lenskit.data.dao.DataAccessException;

/**
 * A database error occurred in the database-backed DAO.
 */
public class DatabaseAccessException extends DataAccessException {
    public DatabaseAccessException() {
    }

    public DatabaseAccessException(String message) {
        super(message);
    }

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseAccessException(Throwable cause) {
        super(cause);
    }
}
