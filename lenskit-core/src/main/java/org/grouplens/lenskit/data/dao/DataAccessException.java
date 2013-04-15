package org.grouplens.lenskit.data.dao;

/**
 * An error occurred during data access. This exception is used to wrap exceptions raised
 * in data access logic.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException() {
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
