/**
 * 
 */
package org.grouplens.reflens.item;

import java.util.Map;

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.data.RatingVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MeanNormalization<S,T> implements Normalization<RatingVector<S,T>> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Normalization#normalize(java.lang.Object)
	 */
	@Override
	public RatingVector<S,T> normalize(RatingVector<S,T> src) {
		RatingVector<S,T> v2 = src.copy();
		float mean = src.getAverage();
		for (Map.Entry<T, Float> e: src.getRatings().entrySet()) {
			v2.putRating(e.getKey(), e.getValue() - mean);
		}
		return v2;
	}
}
