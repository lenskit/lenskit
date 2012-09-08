package org.grouplens.lenskit.core;

import org.grouplens.lenskit.RecommenderBuildException;

/**
 * Error thrown when an error occurs resolving the recommender configuration graph.
 *
 * @since 1.0
 */
public class RecommenderConfigurationException extends RecommenderBuildException {
    public RecommenderConfigurationException() {
    }

    public RecommenderConfigurationException(String message) {
        super(message);
    }

    public RecommenderConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecommenderConfigurationException(Throwable cause) {
        super(cause);
    }

    public RecommenderConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
