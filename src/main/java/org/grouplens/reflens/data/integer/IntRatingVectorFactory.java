/**
 * 
 */
package org.grouplens.reflens.data.integer;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.RatingVectorFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IntRatingVectorFactory<S> implements RatingVectorFactory<S, Integer> {

	@Override
	public RatingVector<S, Integer> make(S owner) {
		return new IntRatingVector<S>(owner);
	}	
}
