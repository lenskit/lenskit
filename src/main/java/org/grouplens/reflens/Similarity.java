/**
 * 
 */
package org.grouplens.reflens;

/**
 * Compute the similarity between two objects (typically rating vectors).
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Similarity<V> {
	/**
	 * Compute the similarity between two vectors.
	 * @param vec1
	 * @param vec2
	 * @return The similarity, in the range [-1,1].
	 */
	float similarity(V vec1, V vec2);
}