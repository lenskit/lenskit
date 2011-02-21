package org.grouplens.reflens.knn;

import org.grouplens.reflens.knn.params.SignificanceThreshold;

/**
 * Significance-weighted variant of Pearson correlation.
 * 
 * This similarity function is like {@link PearsonCorrelation}, but it does
 * similarity weighting.  When applied to user rating vectors, if the number
 * of co-rated items <i>n</i> is less than the threshold <i>T</i>, then the
 * similarity is multiplied by <i>n/T</i>.  This decreases the importance of
 * the similarity between vectors with few ratings in common.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SignificanceWeightedPearsonCorrelation extends PearsonCorrelation {
	private final int threshold;
	
	public SignificanceWeightedPearsonCorrelation(@SignificanceThreshold int thresh) {
		threshold = thresh;
	}
	
	@Override
	protected double computeFinalCorrelation(int nCoratings, double dot, double var1, double var2) {
		double v = super.computeFinalCorrelation(nCoratings, dot, var1, var2);
		if (nCoratings < threshold)
			v *= (double) nCoratings / threshold;
		return v;
	}

}
