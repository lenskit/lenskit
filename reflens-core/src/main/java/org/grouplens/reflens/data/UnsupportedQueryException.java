/**
 * 
 */
package org.grouplens.reflens.data;

/**
 * Exception thrown when a data source receives an unsupported query.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UnsupportedQueryException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1340119072527578247L;

	public UnsupportedQueryException() {
	}

	public UnsupportedQueryException(String message) {
		super(message);
	}

}
