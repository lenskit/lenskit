package org.grouplens.lenskit;

/**
 * Exception thrown when the configured recommender is incompatible with its
 * usage.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IncompatibleRecommenderException extends UnsupportedOperationException {
    private static final long serialVersionUID = -570479509636551006L;

    /**
     *
     */
    public IncompatibleRecommenderException() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public IncompatibleRecommenderException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public IncompatibleRecommenderException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public IncompatibleRecommenderException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
