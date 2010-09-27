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
public class GenericRatingVectorFactory<E, T> implements
		RatingVectorFactory<E, T> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.data.RatingVectorFactory#make(java.lang.Object)
	 */
	@Override
	public RatingVector<E, T> make(E entity) {
		return new GenericRatingVector<E,T>(entity);
	}

}
