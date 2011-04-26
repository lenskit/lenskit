package org.grouplens.lenskit.plsi;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.grouplens.lenskit.AbstractDynamicRatingPredictor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;

public class PLSIRatingPredictor extends AbstractDynamicRatingPredictor {
	
	private final RatingPredictor baseline;
	private final int numFeatures;
	private final PLSIRecommenderBuilder.FeatureModel featureModel;
	private final Index itemIndex;
	private final int numIterations;
	
	public PLSIRatingPredictor (
			RatingDataAccessObject ratingDAO,
			PLSIRecommenderBuilder.FeatureModel featureModel,
			RatingPredictor baseline,
			int numFeatures,
			Index itemIndex,
			int numIterations) {
		super(ratingDAO);
		this.baseline = baseline;
		this.numFeatures = numFeatures;
		this.featureModel = featureModel;
		this.itemIndex = itemIndex;
		this.numIterations = numIterations;
	}

	@Override
	public SparseVector predict(long user, SparseVector ratings,
			Collection<Long> items) {
		double[] userFeatureWeights = new double[numFeatures];
		Random random = new Random();
		for (int feature = 0; feature < numFeatures; feature++) {
			userFeatureWeights[feature] = random.nextDouble();
		}
		PLSIRecommenderBuilder.normalize(userFeatureWeights);
		double[] newUserFeatureWeights = new double[numFeatures];
		double[] arraySwapRef = null;
		int ratedItem = 0;
		double ratingValue = 0;
		// Train user feature weights
		//System.err.println("Training user features");
		for (int i = 0; i < numIterations; i++) {
			//System.err.println(Arrays.toString(userFeatureWeights));
			for (Entry entry : ratings.fast()) {
				ratedItem = itemIndex.getIndex(entry.getLongKey());
				ratingValue = entry.getDoubleValue();
				for (int feature = 0; feature < numFeatures; feature++) {
					newUserFeatureWeights[feature] += featureModel.getProbability(
							userFeatureWeights, ratedItem, ratingValue, feature);
				}
				for (int feature = 0; feature < numFeatures; feature++) {
					newUserFeatureWeights[feature] /= ratings.size();
				}
				PLSIRecommenderBuilder.normalize(newUserFeatureWeights);
				arraySwapRef = newUserFeatureWeights;
				newUserFeatureWeights = userFeatureWeights;
				userFeatureWeights = arraySwapRef;
			}
		}
		
		// Generate scores based on training values
		long[] itemIds = new long[items.size()];
		double[] scores = new double[items.size()];
		int i = 0;
		for (long item : items) {
			itemIds[i] = item;
			scores[i] = featureModel.getRatingPrediction(itemIndex.getIndex(item), userFeatureWeights);
			i++;
		}
		return SparseVector.wrap(itemIds, scores);
	}

}
