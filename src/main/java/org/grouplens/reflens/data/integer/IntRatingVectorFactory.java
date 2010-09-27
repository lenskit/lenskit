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
public class IntRatingVectorFactory<E> implements RatingVectorFactory<E, Integer> {

	@Override
	public RatingVector<E, Integer> make(E entity) {
		return new IntRatingVector<E>(entity);
	}	
}
