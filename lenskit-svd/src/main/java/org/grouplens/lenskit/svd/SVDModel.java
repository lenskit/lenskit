/**
 * 
 */
package org.grouplens.lenskit.svd;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.util.DoubleFunction;

import com.google.inject.ProvidedBy;

/**
 * The SVD model used for recommendation and prediction.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ProvidedBy(SVDModelProvider.class)
public class SVDModel {
	public final int featureCount;
	public final double itemFeatures[][];
	public final double singularValues[];
	public final DoubleFunction clampingFunction;
	
	public final Index itemIndex;
	public final RatingPredictor baseline;
	
	public SVDModel(int nfeatures, double[][] ifeats, double[] svals,
			DoubleFunction clamp, Index iidx, RatingPredictor base) {
		featureCount = nfeatures;
		itemFeatures = ifeats;
		singularValues = svals;
		clampingFunction = clamp;
		itemIndex = iidx;
		baseline = base;
	}
	
	public double itemFeatureValue(int item, int feature) {
	    return itemFeatures[feature][item];
	}
	
	public int getItemIndex(long item) {
	    return itemIndex.getIndex(item);
	}
}
