/*
 * RefLens, a reference implementation of recommender algorithms.
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
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.grouplens.reflens.svd;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.grouplens.reflens.BasketRecommender;
import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingRecommender;
import org.grouplens.reflens.RecommenderEngine;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.SortOrder;
import org.grouplens.reflens.svd.params.FeatureCount;
import org.grouplens.reflens.svd.params.LearningRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Do recommendations using Funk's SVD algorithm.
 * 
 * @todo factor this into an SVD recommender and an SVD model builder, so we can
 * use non-FUNK algorithms.
 * 
 * The implementation of FunkSVD is based on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class FunkSVD implements RecommenderEngine, RatingPredictor {
	private static Logger logger = LoggerFactory.getLogger(FunkSVD.class);
	private static final double DEFAULT_FEATURE_VALUE = 0.1f;
	private static final double FEATURE_EPSILON = 0.0001f;
	private static final double MIN_EPOCHS = 50;
	private static final double TRAINING_BLEND = 0.015f; // Funk's K
	
	private final double learningRate;
	private final int numFeatures;
	
	private Indexer userIndexer;
	private Indexer itemIndexer;

	private double userFeatures[][];
	private double itemFeatures[][];
	private double singularValues[];
	
	private DoubleList itemAverages;
	private DoubleList userAvgOffsets;
	
	@Inject
	FunkSVD(Indexer userIndexer, Indexer itemIndexer,
			@FeatureCount int features,
			@LearningRate double lrate) {
		learningRate = lrate;
		numFeatures = features;
		this.userIndexer = userIndexer;
		this.itemIndexer = itemIndexer;
	}
	
	private void computeItemAverages(Collection<SVDRating> ratings) {
		itemAverages = new DoubleArrayList();
		int ircounts[] = new int[itemIndexer.getObjectCount()];
		itemAverages.size(itemIndexer.getObjectCount());
		double globalAvg = 0.0f;
		for (SVDRating r: ratings) {
			itemAverages.set(r.item, itemAverages.getDouble(r.item) + r.value);
			ircounts[r.item]++;
			globalAvg += r.value;
		}
		globalAvg /= ratings.size();
		for (int i = 0; i < ircounts.length; i++) {
			double avg = globalAvg * 25 + itemAverages.get(i);
			avg = avg / (ircounts[i] + 25);
			itemAverages.set(i, avg);
		}
	}
	
	private void computeUserAverageOffsets(Collection<SVDRating> ratings) {
		userAvgOffsets = new DoubleArrayList();
		int urcounts[] = new int[userIndexer.getObjectCount()];
		userAvgOffsets.size(userIndexer.getObjectCount());
		double globalAvg = 0.0f;
		for (SVDRating r: ratings) {
			double offset = r.value - itemAverages.get(r.item);
			userAvgOffsets.set(r.user, userAvgOffsets.getDouble(r.user) + offset);
			urcounts[r.user]++;
			globalAvg += offset;
		}
		globalAvg /= ratings.size();
		for (int i = 0; i < urcounts.length; i++) {
			double avg = globalAvg * 25 + userAvgOffsets.get(i);
			avg = avg / (urcounts[i] + 25);
			userAvgOffsets.set(i, avg);
		}
	}
	
	void build(RatingDataSource users) {
		logger.debug("Building SVD with {} features", numFeatures);
		
		// build a list of ratings
		Cursor<Rating> cursor = users.getRatings(SortOrder.USER);
		int nusers = cursor.getRowCount();
		List<SVDRating> ratings = new ArrayList<SVDRating>(nusers >= 0 ? nusers * 5 : 100);
		try {
			long user = 0;
			int uid = -1;
			for (Rating rating: cursor) {
				long u = rating.getUserId();
				if (uid < 0 || user != u)
					uid = userIndexer.internId(rating.getUserId());
				int iid = itemIndexer.internId(rating.getItemId());
				SVDRating r = new SVDRating(uid, iid, rating.getRating());
				ratings.add(r);
			}
		} finally {
			cursor.close();
		}
		
		computeItemAverages(ratings);
		computeUserAverageOffsets(ratings);
		
		userFeatures = new double[numFeatures][userIndexer.getObjectCount()];
		itemFeatures = new double[numFeatures][itemIndexer.getObjectCount()];
		for (int feature = 0; feature < numFeatures; feature++) {
			trainFeature(feature, ratings);
		}
		
		logger.debug("Extracting singular values");
		singularValues = new double[numFeatures];
		for (int feature = 0; feature < numFeatures; feature++) {
			double ussq = 0;
			int numUsers = userIndexer.getObjectCount();
			for (int i = 0; i < numUsers; i++) {
				double uf = userFeatures[feature][i];
				ussq += uf * uf;
			}
			double unrm = (double) Math.sqrt(ussq);
			if (unrm > 0.0001f) {
				for (int i = 0; i < numUsers; i++) {
					userFeatures[feature][i] /= unrm;
				}
			}
			double issq = 0;
			int numItems = itemIndexer.getObjectCount();
			for (int i = 0; i < numItems; i++) {
				double fv = itemFeatures[feature][i];
				issq += fv * fv;
			}
			double inrm = (double) Math.sqrt(issq);
			if (inrm > 0.0001f) {
				for (int i = 0; i < numItems; i++) {
					itemFeatures[feature][i] /= inrm;
				}
			}
			singularValues[feature] = unrm * inrm;
		}
	}
	
	private void trainFeature(int feature, Collection<SVDRating> ratings) {
		double ufv[] = userFeatures[feature];
		double ifv[] = itemFeatures[feature];
		DoubleArrays.fill(ufv, DEFAULT_FEATURE_VALUE);
		DoubleArrays.fill(ifv, DEFAULT_FEATURE_VALUE);
				
		logger.debug("Training feature {}", feature);
		
		double rmse = 2.0f, oldRmse = 0.0f;
		int epoch;
		for (epoch = 0; epoch < MIN_EPOCHS || rmse < oldRmse - FEATURE_EPSILON; epoch++) {
			logger.trace("Running epoch {} of feature {}", epoch, feature);
			oldRmse = rmse;
			double ssq = 0;
			for (SVDRating r: ratings) {
				double err = r.value - r.predict(feature);
				ssq += err * err;
				
				// save values
				double ouf = ufv[r.user];
				double oif = ifv[r.item];
				// update user feature preference
				double udelta = err * oif - TRAINING_BLEND * ouf;
				ufv[r.user] += udelta * learningRate;
				// update item feature relevance
				double idelta = err * ouf - TRAINING_BLEND * oif;
				ifv[r.item] += idelta * learningRate;
			}
			rmse = (double) Math.sqrt(ssq / ratings.size());
			logger.trace("Epoch {} had RMSE of {}", epoch, rmse);
		}
		
		logger.debug("Finished feature {} in {} epochs", feature, epoch);
		for (SVDRating r: ratings) {
			r.update(feature);
		}
	}
	
	private class SVDRating {
		public final int user;
		public final int item;
		public final double value;
		private double cachedValue = Double.NaN;
		
		public SVDRating(int user, int item, double value) {
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
		public double predict(int feature) {
			double sum;
			if (Double.isNaN(cachedValue))
				sum = itemAverages.getDouble(item) + userAvgOffsets.getDouble(user);
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
	
	protected double[] foldIn(Map<Long,Double> ratings, double avgDeviation) {
		double featurePrefs[] = new double[numFeatures];
		DoubleArrays.fill(featurePrefs, 0.0f);
		
		for (ScoredId rating: ScoredId.fastWrap(ratings)) {
			int iid = itemIndexer.getIndex(rating.getId());
			if (iid < 0) continue;
			double r = rating.getScore() - avgDeviation - itemAverages.get(iid);
			for (int f = 0; f < numFeatures; f++) {
				featurePrefs[f] += r * itemFeatures[f][iid] / singularValues[f];
			}
		}
		
		return featurePrefs;
	}
	
	protected double averageDeviation(Map<Long,Double> ratings) {
		double dev = 0.0f;
		int n = 0;
		for (ScoredId rating: ScoredId.fastWrap(ratings)) {
			int iid = itemIndexer.getIndex(rating.getId());
			if (iid < 0) continue;
			dev += rating.getScore() - itemAverages.getDouble(iid);
			n++;
		}
		if (n == 0)
			return 0.0f;
		else
			return dev / n;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.RecommenderEngine#predict(org.grouplens.reflens.data.UserRatingProfile, java.lang.Object)
	 */
	@Override
	public ScoredId predict(long user, Map<Long, Double> ratings, long item) {
		LongArrayList items = new LongArrayList(1);
		items.add(item);
		Map<Long,Double> scores = predict(user, ratings, items);
		
		if (scores.containsKey(item))
			return new ScoredId(item, scores.get(item));
		else
			return null;
	}
	
	@Override
	public Map<Long,Double> predict(long user, Map<Long, Double> ratings, Collection<Long> items) {
		double dev = averageDeviation(ratings);
		double uprefs[] = foldIn(ratings, dev);
		
		Long2DoubleMap preds = new Long2DoubleOpenHashMap();
		for (long item: items) {
			int iid = itemIndexer.getIndex(item);
			if (iid < 0)
				continue;

			double score = itemAverages.get(iid) + dev;
			for (int f = 0; f < numFeatures; f++) {
				score += uprefs[f] * singularValues[f] * itemFeatures[f][iid];
			}
			preds.put(item, score);
		}
		return preds;
	}

	@Override
	public BasketRecommender getBasketRecommender() {
		return null;
	}

	@Override
	public RatingPredictor getRatingPredictor() {
		return this;
	}

	@Override
	public RatingRecommender getRatingRecommender() {
		return null;
	}
}
