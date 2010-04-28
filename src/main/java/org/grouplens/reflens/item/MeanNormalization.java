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
public class MeanNormalization<I> implements Normalization<RatingVector<I>> {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Normalization#normalize(java.lang.Object)
	 */
	@Override
	public RatingVector<I> normalize(RatingVector<I> src) {
		RatingVector<I> v2 = src.copy();
		float mean = src.getAverage();
		for (Map.Entry<I, Float> e: src.getRatings().entrySet()) {
			v2.putRating(e.getKey(), e.getValue() - mean);
		}
		return v2;
	}
}
