/**
 * 
 */
package org.grouplens.reflens.item;

import java.util.Map;

import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.RatingVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CosineSimilarity<I, V extends RatingVector<?,I>>
	implements Similarity<V>, SymmetricBinaryFunction {

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Similarity#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public float similarity(V vec1, V vec2) {
		float dot = 0.0f;
		float ssq1 = 0.0f;
		float ssq2 = 0.0f;
		for (Map.Entry<I,Float> e: vec1.getRatings().entrySet()) {
			I i = e.getKey();
			float v = e.getValue();
			if (vec2.containsObject(i)) {
				dot += v * vec2.getRating(i);
			}
			ssq1 += v * v;
		}
		for (Float v: vec2.getRatings().values()) {
			ssq2 += v * v;
		}
		double denom = Math.sqrt(ssq1) * Math.sqrt(ssq2);
		if (denom == 0.0f) {
			return Float.NaN;
		} else { 
			return dot / (float) denom;
		}
	}

}
