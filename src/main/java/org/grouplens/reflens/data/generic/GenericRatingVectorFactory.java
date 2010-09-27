/**
 * 
 */
package org.grouplens.reflens.data.generic;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.RatingVectorFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GenericRatingVectorFactory<S, T> implements
		RatingVectorFactory<S, T> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingVectorFactory#make(java.lang.Object)
	 */
	@Override
	public RatingVector<S, T> make(S owner) {
		return new GenericRatingVector<S,T>(owner);
	}

}
