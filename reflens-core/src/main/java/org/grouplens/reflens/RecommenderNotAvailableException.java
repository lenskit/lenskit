package org.grouplens.reflens;

/**
 * Exception thrown when a recommender is not available.
 * 
 * <p>Recommenders can be unavailable for a variety of reasons: there could be
 * no recommender in a cache and no means to build one, or there could be an
 * error building the recommender, or any of a variety of problems.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class RecommenderNotAvailableException extends Exception {
	private static final long serialVersionUID = 7518432427712149396L;

	/**
	 * 
	 */
	public RecommenderNotAvailableException() {
	}

	/**
	 * @param message
	 */
	public RecommenderNotAvailableException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public RecommenderNotAvailableException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecommenderNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
