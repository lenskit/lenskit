/**
 * 
 */
package org.grouplens.reflens.svd;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.data.DataFactory;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do recommendations using Funk's SVD algorithm.
 * 
 * TODO: factor this into an SVD recommender and an SVD model builder, so we can
 * use non-FUNK algorithms.
 * 
 * The implementation of FunkSVD is based on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class FunkSVD<U, I> implements Recommender<U, I> {
	private static Logger logger = LoggerFactory.getLogger(FunkSVD.class);
	private static final float DEFAULT_FEATURE_VALUE = 0.1f;
	private static final float FEATURE_EPSILON = 0.0001f;
	private static final float MIN_EPOCHS = 50;
	private static final float TRAINING_BLEND = 0.015f; // Funk's K
	
	private DataFactory<U,I> dataFactory;
	private final float learningRate;
	private final int numFeatures;
	
	private Indexer<U> userIndexer;
	private Indexer<I> itemIndexer;

	private float userFeatures[][];
	private float itemFeatures[][];
	
	private FloatList itemAverages;
	private FloatList userAvgOffsets;
	
	public FunkSVD(DataFactory<U,I> factory, Collection<RatingVector<U,I>> users,
			int features, float lrate) {
		dataFactory = factory;
		learningRate = lrate;
		numFeatures = features;
		build(users);
	}
	
	private void computeItemAverages(Collection<Rating> ratings) {
		itemAverages = new FloatArrayList();
		int ircounts[] = new int[itemIndexer.getObjectCount()];
		itemAverages.size(itemIndexer.getObjectCount());
		float globalAvg = 0.0f;
		for (Rating r: ratings) {
			itemAverages.set(r.item, itemAverages.getFloat(r.item) + r.value);
			ircounts[r.item]++;
			globalAvg += r.value;
		}
		globalAvg /= ratings.size();
		for (int i = 0; i < ircounts.length; i++) {
			float avg = globalAvg * 25 + itemAverages.get(i);
			avg = avg / (ircounts[i] + 25);
			itemAverages.set(i, avg);
		}
	}
	
	private void computeUserAverageOffsets(Collection<Rating> ratings) {
		userAvgOffsets = new FloatArrayList();
		int urcounts[] = new int[userIndexer.getObjectCount()];
		userAvgOffsets.size(userIndexer.getObjectCount());
		float globalAvg = 0.0f;
		for (Rating r: ratings) {
			float offset = r.value - itemAverages.get(r.item);
			userAvgOffsets.set(r.user, userAvgOffsets.getFloat(r.user) + offset);
			urcounts[r.user]++;
			globalAvg += offset;
		}
		globalAvg /= ratings.size();
		for (int i = 0; i < urcounts.length; i++) {
			float avg = globalAvg * 25 + userAvgOffsets.get(i);
			avg = avg / (urcounts[i] + 25);
			userAvgOffsets.set(i, avg);
		}
	}
	
	private void build(Collection<RatingVector<U,I>> users) {
		logger.debug("Building SVD with {} features", numFeatures);
		itemIndexer = dataFactory.makeItemIndexer();
		userIndexer = dataFactory.makeUserIndexer();
		
		// build a list of ratings
		List<Rating> ratings = new ArrayList<Rating>(users.size() * 5);
		for (RatingVector<U,I> user: users) {
			int uid = userIndexer.getIndex(user.getOwner());
			for (ObjectValue<I> rating: user) {
				int iid = itemIndexer.getIndex(rating.getItem());
				Rating r = new Rating(uid, iid, rating.getRating());
				ratings.add(r);
			}
		}
		
		computeItemAverages(ratings);
		computeUserAverageOffsets(ratings);
		
		userFeatures = new float[numFeatures][userIndexer.getObjectCount()];
		itemFeatures = new float[numFeatures][itemIndexer.getObjectCount()];
		for (int feature = 0; feature < numFeatures; feature++) {
			trainFeature(feature, ratings);
		}
	}
	
	private void trainFeature(int feature, Collection<Rating> ratings) {
		float ufv[] = userFeatures[feature];
		float ifv[] = itemFeatures[feature];
		FloatArrays.fill(ufv, DEFAULT_FEATURE_VALUE);
		FloatArrays.fill(ifv, DEFAULT_FEATURE_VALUE);
				
		logger.debug("Training feature {}", feature);
		
		float rmse = 2.0f, oldRmse = 0.0f;
		int epoch;
		for (epoch = 0; epoch < MIN_EPOCHS || rmse < oldRmse - FEATURE_EPSILON; epoch++) {
			logger.trace("Running epoch {} of feature {}", epoch, feature);
			oldRmse = rmse;
			float ssq = 0;
			for (Rating r: ratings) {
				float err = r.value - r.predict(feature);
				ssq += err * err;
				
				// save values
				float ouf = ufv[r.user];
				float oif = ifv[r.item];
				// update user feature preference
				float udelta = err * oif - TRAINING_BLEND * ouf;
				ufv[r.user] += udelta * learningRate;
				// update item feature relevance
				float idelta = err * ouf - TRAINING_BLEND * oif;
				ifv[r.item] += idelta * learningRate;
			}
			rmse = (float) Math.sqrt(ssq / ratings.size());
			logger.trace("Epoch {} had RMSE of {}", epoch, rmse);
		}
		
		logger.debug("Finished feature {} in {} epochs", feature, epoch);
		for (Rating r: ratings) {
			r.update(feature);
		}
	}
	
	private class Rating {
		public final int user;
		public final int item;
		public final float value;
		private float cachedValue = Float.NaN;
		
		public Rating(int user, int item, float value) {
			this.user = user;
			this.item = item;
			this.value = value;
		}
		
		/**
		 * Predict the value up through a particular feature, using the cache
		 * if possible.
		 * @param feature
		 * @return
		 */
		public float predict(int feature) {
			float sum;
			if (Float.isNaN(cachedValue))
				sum = itemAverages.getFloat(item) + userAvgOffsets.getFloat(user);
			else
				sum = cachedValue;
			
			sum += itemFeatures[feature][item] * userFeatures[feature][user];
			sum += (numFeatures - feature - 1) * (DEFAULT_FEATURE_VALUE * DEFAULT_FEATURE_VALUE);
			return sum;
		}
		
		/**
		 * Update the cached prediction using a feature.
		 * @param feature The feature to use.  No more feature predictions
		 * should be done with it.
		 */
		public void update(int feature) {
			cachedValue = predict(feature);
		}
	}
	
	protected float[] foldIn(RatingVector<U,I> user, float avgDeviation) {
		float featurePrefs[] = new float[numFeatures];
		FloatArrays.fill(featurePrefs, 0.0f);
		
		for (ObjectValue<I> rating: user) {
			int iid = itemIndexer.getIndex(rating.getItem(), false);
			if (iid < 0) continue;
			float r = rating.getRating() - avgDeviation - itemAverages.get(iid);
			for (int f = 0; f < numFeatures; f++) {
				featurePrefs[f] += r * itemFeatures[f][iid];
			}
		}
		
		return featurePrefs;
	}
	
	protected float averageDeviation(RatingVector<U,I> user) {
		float dev = 0.0f;
		int n = 0;
		for (ObjectValue<I> rating: user) {
			int iid = itemIndexer.getIndex(rating.getItem(), false);
			if (iid < 0) continue;
			dev += rating.getRating() - itemAverages.getFloat(iid);
			n++;
		}
		if (n == 0)
			return 0.0f;
		else
			return dev / n;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Recommender#predict(org.grouplens.reflens.data.RatingVector, java.lang.Object)
	 */
	@Override
	public ObjectValue<I> predict(RatingVector<U, I> user, I item) {
		float dev = averageDeviation(user);
		float uprefs[] = foldIn(user, dev);
		int iid = itemIndexer.getIndex(item, false);
		if (iid < 0)
			return null;
		
		float score = itemAverages.get(iid) + dev;
		for (int f = 0; f < numFeatures; f++) {
			score += uprefs[f] * itemFeatures[f][iid];
		}
		return new ObjectValue<I>(item, score);
	}
	
	public Map<I,Float> predict(RatingVector<U,I> user, Set<I> items) {
		float adev = averageDeviation(user);
		float uprefs[] = foldIn(user, adev);

		Map<I,Float> results = dataFactory.makeItemFloatMap();
		
		for (I item: items) {
			int iid = itemIndexer.getIndex(item, false);
			if (iid < 0) continue;
			float score = itemAverages.get(iid) + adev;
			for (int f = 0; f < numFeatures; f++) {
				score += uprefs[f] * itemFeatures[f][iid];
			}
			results.put(item, score);
		}
		
		return results;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Recommender#recommend(org.grouplens.reflens.data.RatingVector)
	 */
	@Override
	public List<ObjectValue<I>> recommend(RatingVector<U, I> user) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Recommender#recommend(java.util.Set)
	 */
	@Override
	public List<ObjectValue<I>> recommend(Set<I> basket) {
		// TODO Auto-generated method stub
		return null;
	}

}
