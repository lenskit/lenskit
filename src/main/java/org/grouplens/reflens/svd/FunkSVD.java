/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommendationEngine;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.ScoredObject;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.svd.params.FeatureCount;
import org.grouplens.reflens.svd.params.LearningRate;
import org.grouplens.reflens.util.Cursor;
import org.grouplens.reflens.util.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

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
public class FunkSVD<U, I> implements RecommendationEngine<U,I>, RatingPredictor<U,I> {
	private static Logger logger = LoggerFactory.getLogger(FunkSVD.class);
	private static final float DEFAULT_FEATURE_VALUE = 0.1f;
	private static final float FEATURE_EPSILON = 0.0001f;
	private static final float MIN_EPOCHS = 50;
	private static final float TRAINING_BLEND = 0.015f; // Funk's K
	
	private final float learningRate;
	private final int numFeatures;
	
	private Index<U> userIndexer;
	private Indexer<I> itemIndexer;
	private Provider<Map<I,Float>> itemMapProvider;

	private float userFeatures[][];
	private float itemFeatures[][];
	private float singularValues[];
	
	private FloatList itemAverages;
	private FloatList userAvgOffsets;
	
	@Inject
	FunkSVD(Index<U> userIndexer, Indexer<I> itemIndexer,
			Provider<Map<I,Float>> itemMapProvider, 
			@FeatureCount int features,
			@LearningRate float lrate) {
		learningRate = lrate;
		numFeatures = features;
		this.userIndexer = userIndexer;
		this.itemIndexer = itemIndexer;
		this.itemMapProvider = itemMapProvider;
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
	
	void build(DataSource<UserRatingProfile<U,I>> users) {
		logger.debug("Building SVD with {} features", numFeatures);
		
		// build a list of ratings
		List<Rating> ratings = new ArrayList<Rating>(users.getRowCount() * 5);
		Cursor<UserRatingProfile<U, I>> cursor = users.cursor();
		try {
			for (UserRatingProfile<U,I> user: cursor) {
				int uid = userIndexer.getIndex(user.getUser());
				for (ScoredObject<I> rating: ScoredObject.fastWrap(user.getRatings())) {
					int iid = itemIndexer.getIndex(rating.getObject());
					Rating r = new Rating(uid, iid, rating.getScore());
					ratings.add(r);
				}
			}
		} finally {
			cursor.close();
		}
		
		computeItemAverages(ratings);
		computeUserAverageOffsets(ratings);
		
		userFeatures = new float[numFeatures][userIndexer.getObjectCount()];
		itemFeatures = new float[numFeatures][itemIndexer.getObjectCount()];
		for (int feature = 0; feature < numFeatures; feature++) {
			trainFeature(feature, ratings);
		}
		
		logger.debug("Extracting singular values");
		singularValues = new float[numFeatures];
		for (int feature = 0; feature < numFeatures; feature++) {
			float ussq = 0;
			int numUsers = userIndexer.getObjectCount();
			for (int i = 0; i < numUsers; i++) {
				float uf = userFeatures[feature][i];
				ussq += uf * uf;
			}
			float unrm = (float) Math.sqrt(ussq);
			if (unrm > 0.0001f) {
				for (int i = 0; i < numUsers; i++) {
					userFeatures[feature][i] /= unrm;
				}
			}
			float issq = 0;
			int numItems = itemIndexer.getObjectCount();
			for (int i = 0; i < numItems; i++) {
				float fv = itemFeatures[feature][i];
				issq += fv * fv;
			}
			float inrm = (float) Math.sqrt(issq);
			if (inrm > 0.0001f) {
				for (int i = 0; i < numItems; i++) {
					itemFeatures[feature][i] /= inrm;
				}
			}
			singularValues[feature] = unrm * inrm;
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
	
	protected float[] foldIn(Map<I,Float> ratings, float avgDeviation) {
		float featurePrefs[] = new float[numFeatures];
		FloatArrays.fill(featurePrefs, 0.0f);
		
		for (ScoredObject<I> rating: ScoredObject.fastWrap(ratings)) {
			int iid = itemIndexer.getIndex(rating.getObject());
			if (iid < 0) continue;
			float r = rating.getScore() - avgDeviation - itemAverages.get(iid);
			for (int f = 0; f < numFeatures; f++) {
				featurePrefs[f] += r * itemFeatures[f][iid] / singularValues[f];
			}
		}
		
		return featurePrefs;
	}
	
	protected float averageDeviation(Map<I,Float> ratings) {
		float dev = 0.0f;
		int n = 0;
		for (ScoredObject<I> rating: ScoredObject.fastWrap(ratings)) {
			int iid = itemIndexer.getIndex(rating.getObject());
			if (iid < 0) continue;
			dev += rating.getScore() - itemAverages.getFloat(iid);
			n++;
		}
		if (n == 0)
			return 0.0f;
		else
			return dev / n;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Recommender#predict(org.grouplens.reflens.data.UserRatingProfile, java.lang.Object)
	 */
	@Override
	public ScoredObject<I> predict(U user, Map<I, Float> ratings, I item) {
		float dev = averageDeviation(ratings);
		float uprefs[] = foldIn(ratings, dev);
		int iid = itemIndexer.getIndex(item);
		if (iid < 0)
			return null;
		
		float score = itemAverages.get(iid) + dev;
		for (int f = 0; f < numFeatures; f++) {
			score += uprefs[f] * singularValues[f] * itemFeatures[f][iid];
		}
		return new ScoredObject<I>(item, score);
	}
	
	public Map<I,Float> predict(UserRatingProfile<U,I> user, Set<I> items) {
		float adev = averageDeviation(user.getRatings());
		float uprefs[] = foldIn(user.getRatings(), adev);

		Map<I,Float> results = itemMapProvider.get();
		
		for (I item: items) {
			int iid = itemIndexer.getIndex(item);
			if (iid < 0) continue;
			float score = itemAverages.get(iid) + adev;
			for (int f = 0; f < numFeatures; f++) {
				score += uprefs[f] * singularValues[f] * itemFeatures[f][iid];
			}
			results.put(item, score);
		}
		
		return results;
	}

	@Override
	public BasketRecommender<U, I> getBasketRecommender() {
		return null;
	}

	@Override
	public RatingPredictor<U, I> getRatingPredictor() {
		return this;
	}

	@Override
	public RatingRecommender<U, I> getRatingRecommender() {
		return null;
	}
}
