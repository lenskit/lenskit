package org.grouplens.lenskit.plsi;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap.Entry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.grouplens.lenskit.DynamicRatingPredictor;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.Index;
import org.grouplens.lenskit.data.IndexedRating;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.context.PackedRatingBuildContext;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.data.context.RatingSnapshot;
import org.grouplens.lenskit.data.dao.SimpleFileDAO;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.util.FastCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PLSIRecommenderBuilder {
    private static Logger logger = LoggerFactory.getLogger(PLSIRecommenderBuilder.class);
    
    final int numFeatures;
    final int iterationCount;
    final double eta;
    final int numOnlineIterations;
    
    public static void main(String[] args) throws IOException {
		File testDataFile = new File("src/test/java/movielens100k.tsv");
		SimpleFileDAO dao = new SimpleFileDAO(testDataFile, "\t");
		File smallTestDataFile = new File("src/test/java/PLSISimpleTestData.csv");
		SimpleFileDAO sdao = new SimpleFileDAO(smallTestDataFile, ",");
		RatingBuildContext ratings = PackedRatingBuildContext.make(dao);
		RatingBuildContext smallRatings = PackedRatingBuildContext.make(sdao);
		PLSIRecommenderBuilder builder = new PLSIRecommenderBuilder(8, 40, 5, 1.0);
		//RatingPredictor predictor = builder.build(ratings, null);
		DynamicRatingPredictor predictor = builder.build(smallRatings, null);
		long[] items = {168, 195, 200, 202, 242, 591, 708};
		double[] userRatings = {4, 4, 1, 4, 5, 5, 3};
		long[] smallItems = {0, 3};
		double[] smallUserRatings = {-1.5, 0.75};
		SparseVector ratingVector = SparseVector.wrap(items, userRatings);
		SparseVector smallRatingVector = SparseVector.wrap(smallItems, smallUserRatings);
		ArrayList<Long> itemsToRate = new ArrayList<Long>();
		ArrayList<Long> smallItemsToRate = new ArrayList<Long>();
		
		itemsToRate.add(Long.valueOf(28));
		itemsToRate.add(Long.valueOf(50));
		itemsToRate.add(Long.valueOf(416));
		
		smallItemsToRate.add(Long.valueOf(2));
		smallItemsToRate.add(Long.valueOf(4));
		
		//SparseVector predictions = predictor.predict(-1, ratingVector, itemsToRate);
		SparseVector predictions = predictor.predict(-1, smallRatingVector, smallItemsToRate);
		for (Entry entry : predictions.fast()) {
			System.err.println("Rating for item " + entry.getLongKey() +
					": " + entry.getDoubleValue());
		}
    }
    
    public PLSIRecommenderBuilder(
    		int featureCount,
    		int iterationCount,
    		int numOnlineIterations,
    		double eta) {
    	this.numFeatures = featureCount;
    	this.iterationCount = iterationCount;
    	this.eta = eta;
    	this.numOnlineIterations = numOnlineIterations;
    }
    
    private static double rmseEvaluatePredictor(DynamicRatingPredictor predictor, RatingSnapshot tuningData) {
    	double trainingPercentage = 0.8;
		Collection<Rating> trainingRatings = new ArrayList<Rating>();
		Map<Long, Double> testingRatings = new HashMap<Long, Double>();
		SparseVector predictions;
		int n = 0;
		double sse = 0;
    	for (long user : tuningData.getUserIds()) {
    		trainingRatings.clear();
    		testingRatings.clear();
    		FastCollection<IndexedRating> ratings = tuningData.getUserRatings(user);
    		int numTrainingRatings = (int)(ratings.size() * trainingPercentage);
    		for (IndexedRating ir : ratings.fast()) {
    			if (trainingRatings.size() >= numTrainingRatings) {
    				testingRatings.put(ir.getItemId(), ir.getRating());
    			} else {
    				trainingRatings.add(ir);
    			}
    		}
    		predictions = predictor.predict(user, trainingRatings, testingRatings.keySet());
    		for (Entry rating : predictions) {
    			double err = rating.getDoubleValue() - testingRatings.get(rating.getLongKey());
    			sse += err * err;
    			n++;
    		}
    	}
    	return Math.sqrt(sse / n);
    }
    
    public DynamicRatingPredictor build(RatingBuildContext buildContext, RatingPredictor baseline) {
    	// TODO: Hold out some ratings for tempering
    	DynamicRatingPredictor predictor = null;
    	FeatureModel featureModel = null;
    	double[][] userFeatureWeights = null;
    	ItemRatingModel itemRatingModel = null;
    	RatingSnapshot trainingData = buildContext.trainingSnapshot();
    	RatingSnapshot tuningData = buildContext.tuningSnapshot();
    	double beta = 1;
    	double lastRoundRMSE = Double.POSITIVE_INFINITY;
    	for (int iteration = 0; iteration < iterationCount; iteration++) {
    		userFeatureWeights = generateUserFeatureWeights(featureModel, trainingData);
    		System.err.println("User feature weights");
    		for (int i = 0; i < 4; i++) {
    			System.err.print("User " + i + ": ");
    			System.err.println(Arrays.toString(userFeatureWeights[i]));
    		}
    		itemRatingModel = new ItemRatingModel(featureModel, trainingData);
    		
			System.err.println("Means:");
    		for (int i = 0; i < 5; i++) {
    			System.err.println(Arrays.toString(itemRatingModel.mean[i]));
    		}
			System.err.println("Variances");
    		for (int i = 0; i < 5; i++) {
    			System.err.println(Arrays.toString(itemRatingModel.variance[i]));
    		}
    		
    		featureModel = new FeatureModel(userFeatureWeights, itemRatingModel, beta);
    		double[] shtuff = new double[numFeatures];
    		for (IndexedRating rating : trainingData.getUserRatings(0)) {
    			featureModel.addProbabilities(shtuff, 0, rating.getItemIndex(), rating.getRating());
    			
    			System.err.println("User 0 qi for " + rating.getItemId() + 
    					" rated " + rating.getRating() + ": " + Arrays.toString(shtuff));
    			Arrays.fill(shtuff, 0);
    		}
    		predictor = new PLSIRatingPredictor(
        			trainingData.getDAO(),
        			featureModel,
        			baseline,
        			numFeatures,
        			trainingData.itemIndex(),
        			numOnlineIterations);
    		double rmse = rmseEvaluatePredictor(predictor, tuningData);
    		System.out.println("rmse = " + rmse);
    		if (rmse >= lastRoundRMSE) {
    			beta *= eta;
    			System.out.println("beta updated to " + beta);
    		}
    		lastRoundRMSE = rmse;
    		//TODO: Q should have a beta parameter, and also, need to be able to test value of current model
    	}
    	return predictor;
    }
    
    // M-step part a
    double[][] generateUserFeatureWeights(FeatureModel featureModel, RatingSnapshot ratings) {
    	Index userIndex = ratings.userIndex();
    	int numUsers = userIndex.getObjectCount();
    	double[][] p = new double[numUsers][numFeatures];
    	
    	// base case: return uniform distribution
    	if (featureModel == null) {
    		Random random = new Random();
    		double sum = 0;
    		for (int user = 0; user < numUsers; user++) {
    			// Create randomly distributed probability vector
    			for (int feature = 0; feature < numFeatures; feature++) {
    				p[user][feature] = random.nextDouble();
    				sum += p[user][feature];
    			}
    			// Normalize it
    			for (int feature = 0; feature < numFeatures; feature++) {
    				p[user][feature] /= sum;
    			}
    		}
    		return p;
    	}
    	// real case: return average contribution from each rating according to model
    	for (int user = 0; user < numUsers; user++) {
    		FastCollection<IndexedRating> userRatings = ratings.getUserRatings(userIndex.getId(user));
			Arrays.fill(p[user], 0);
    		for (IndexedRating rating : userRatings.fast()) {
    			featureModel.addProbabilities(p[user], user, rating.getItemIndex(), rating.getRating());
    		}
    		for (int feature = 0; feature < numFeatures; feature++) {
    			p[user][feature] /= userRatings.size();
    		}
    		normalize(p[user]);
    	}
    	return p;
    }

    
    // E-step
    class FeatureModel {
    	
    	double[][] userFeatureWeights;
    	ItemRatingModel itemRatingModel;
    	double beta;
    	
    	//TODO: if iterating across features, denominators will be reused. Refactor this to optimize.
    	double getProbability(double[] userFeatureWeights, int item, double rating, int feature) {
    		double numer = userFeatureWeights[feature] * itemRatingModel.getProbability(rating, item, feature);
    		if (numer == 0) return 0;
    		double denom = 0;
    		for (int otherFeature = 0; otherFeature < numFeatures; otherFeature++) {
    			denom += userFeatureWeights[otherFeature] * itemRatingModel.getProbability(rating, item, otherFeature);
    		}
    		return numer / denom;
    	}
    	
    	void addProbabilities(double[] userFeatureWeightsToUpdate, int user, int item, double rating) {
    		assert userFeatureWeightsToUpdate.length == numFeatures;
    		double numer, denom = 0;
    		for (int otherFeature = 0; otherFeature < numFeatures; otherFeature++) {
    			double value = userFeatureWeights[user][otherFeature] * itemRatingModel.getProbability(rating, item, otherFeature);
    			denom += Math.pow(value, beta);
    		}
    		for (int feature = 0; feature < numFeatures; feature++) {
    			numer = userFeatureWeights[user][feature] * itemRatingModel.getProbability(rating, item, feature);
    			numer = Math.pow(numer, beta);
    			userFeatureWeightsToUpdate[feature] += (numer == 0? numer : numer / denom);
    		}
    	}
    	
    	@Deprecated
    	double getProbability(int user, int item, double rating, int feature) {
    		double numer = userFeatureWeights[user][feature] * itemRatingModel.getProbability(rating, item, feature);
    		double denom = 0;
    		for (int otherFeature = 0; otherFeature < numFeatures; otherFeature++) {
    			denom += userFeatureWeights[user][otherFeature] * itemRatingModel.getProbability(rating, item, otherFeature);
    		}
    		return numer / denom;
    	}
    	
    	double getRatingPrediction(int item, double[] userFeatureWeights) {
    		//TODO: something else for where item < 0 or item > numItems;
    		assert userFeatureWeights.length == numFeatures;
    		double rating = 0;
    		for (int feature = 0; feature < numFeatures; feature++) {
    			//TODO: Should this somehow be weighted by the variance?
    			rating += this.itemRatingModel.mean[item][feature] * userFeatureWeights[feature];
    		}
    		return rating;
    	}
    	
    	FeatureModel (double[][] userFeatureWeights, ItemRatingModel itemRatingModel, double beta) { //TODO: precompute any of this?
    		this.userFeatureWeights = userFeatureWeights;
    		this.itemRatingModel = itemRatingModel;
    		this.beta = beta;
    	}

    }
    
    class ItemRatingModel {
    	
    	double[][] mean;
    	double[][] variance;
    	
    	// M-step part b
    	ItemRatingModel(FeatureModel featureModel, RatingSnapshot ratings) {
    		int numItems = ratings.itemIndex().getObjectCount();
    		System.err.println("Building new ItemRatingModel with " + numItems + " items and FeatureModel " + featureModel);
    		this.mean = new double[numItems][numFeatures];
    		this.variance = new double[numItems][numFeatures];
    		double[][] denom = new double[numItems][numFeatures];
    		double[] featureWeights = new double[numFeatures];
    		double ratingValue;
    		int user, item;
			for (IndexedRating rating : ratings.getRatings().fast()) {
    			user = rating.getUserIndex();
    			item = rating.getItemIndex();
    			ratingValue = rating.getRating();
    			Arrays.fill(featureWeights, 0);
    			if (featureModel == null) {
    				Arrays.fill(featureWeights, 1.0 / numFeatures);
    			} else {
    				featureModel.addProbabilities(featureWeights, user, item, ratingValue);
    			}
    			for (int feature = 0; feature < numFeatures; feature++) {
	    			mean[item][feature] += featureWeights[feature] * ratingValue;
	    			denom[item][feature] += featureWeights[feature];
    			}
			}
			for (item = 0; item < numItems; item++) {
				for (int feature = 0; feature < numFeatures; feature++) {
					mean[item][feature] /= denom[item][feature];
				}
			}
			for (IndexedRating rating : ratings.getRatings().fast()) {
    			user = rating.getUserIndex();
    			item = rating.getItemIndex();
    			ratingValue = rating.getRating();
    			Arrays.fill(featureWeights, 0);
    			if (featureModel == null) {
    				Arrays.fill(featureWeights, 1.0 / numFeatures);
    			} else {
    				featureModel.addProbabilities(featureWeights, user, item, ratingValue);
    			}
    			for (int feature = 0; feature < numFeatures; feature++) {
	    			variance[item][feature] += featureWeights[feature] * Math.pow(ratingValue - mean[item][feature], 2);
	    		}
			}
			for (item = 0; item < numItems; item++) {
				for (int feature = 0; feature < numFeatures; feature++) {
					variance[item][feature] /= denom[item][feature];
				}
			}
    		/* Essentially this, with loop order reversed to accommodate accessible API
    		 * for z in features
    		 * 	for y in items
    		 * 		double n = 0; double d = 0;
    		 * 		for (u, v) in y ratings
    		 * 			double qi = q.getProbability(u, y, w, v)
    		 * 			n += gi * v;
    		 * 			d += gi;
    		 * 		mean[y][z] = n / d;
    		 * 		n = 0; d = 0;
    		 * 		for (u, v) in y ratings
    		 * 			double qi = q.getProbability(u, y, w, v)
    		 * 			n += gi * (v - mean[y][z])^2;
    		 * 			d += gi;
    		 * 		variance[y][z] = n / d;
    		 */
    	}
    	
    	double getProbability(double rating, int item, int feature) {
    		double mu = mean[item][feature];
    		double sigma = variance[item][feature];
    		if (sigma == 0) return (rating == mu? 1 : 0); //TODO: log something? Probably an error
    		// Now use normal curve probability density function
    		return 1.0 / Math.sqrt(2 * Math.PI * sigma) * Math.exp(-1.0 *  Math.pow((rating - mu), 2) / (2 * sigma));
    	}
    }
    
    static void normalize(double[] values) {
    	double sum = 0;
    	for (double val : values) {sum += val;}
    	for (int i = 0; i < values.length; i++) {values[i] /= sum;}
    }
    
}