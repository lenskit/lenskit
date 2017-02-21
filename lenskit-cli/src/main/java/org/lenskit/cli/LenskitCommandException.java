package org.lenskit.cli;

/**
 * Exception thrown when a LensKit command fails.
 */
public class LenskitCommandException extends Exception {
    public LenskitCommandException() {
    }

    public LenskitCommandException(String message) {
        super(message);
    }

    public LenskitCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    public LenskitCommandException(Throwable cause) {
        super(cause);
    }

    public LenskitCommandException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
