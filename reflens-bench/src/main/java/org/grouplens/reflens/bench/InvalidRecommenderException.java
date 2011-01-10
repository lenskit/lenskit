package org.grouplens.reflens.bench;

/**
 * Raised if the recommender cannot be created for some reason.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class InvalidRecommenderException extends Exception {

	/**
	 * 
	 */
	public InvalidRecommenderException() {
	}

	/**
	 * @param message
	 */
	public InvalidRecommenderException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidRecommenderException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidRecommenderException(String message, Throwable cause) {
		super(message, cause);
	}

}
