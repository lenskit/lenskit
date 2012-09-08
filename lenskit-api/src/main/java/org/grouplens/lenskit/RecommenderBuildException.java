package org.grouplens.lenskit;

/**
 * Exception thrown when there is an error building a recommender.
 *
 * @since 1.0
 */
public class RecommenderBuildException extends Exception {
    public RecommenderBuildException() {
    }

    public RecommenderBuildException(String message) {
        super(message);
    }

    public RecommenderBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecommenderBuildException(Throwable cause) {
        super(cause);
    }

    public RecommenderBuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
