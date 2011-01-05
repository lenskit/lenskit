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

import it.unimi.dsi.fastutil.doubles.DoubleArrays;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RatingPredictorBuilder;
import org.grouplens.reflens.RecommenderEngine;
import org.grouplens.reflens.RecommenderEngineBuilder;
import org.grouplens.reflens.baseline.ConstantPredictor;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.params.BaselinePredictor;
import org.grouplens.reflens.svd.params.FeatureCount;
import org.grouplens.reflens.svd.params.LearningRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * SVD recommender builder using gradient descent (Funk SVD).
 * 
 * This recommender builder constructs an SVD-based recommender using gradient
 * descent, as pioneered by Simon Funk.  It also incorporates the regularizations
 * Funk did.  These are documented in
 * <a href="http://sifter.org/~simon/journal/20061211.html">Netflix Update: Try
 * This at Home</a>.
 * 
 * This implementation is based in part on
 * <a href="http://www.timelydevelopment.com/demos/NetflixPrize.aspx">Timely
 * Development's sample code</a>.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class GradientDescentSVDRecommenderBuilder implements RecommenderEngineBuilder {
	private static Logger logger = LoggerFactory.getLogger(GradientDescentSVDRecommenderBuilder.class);
	
	private static final double DEFAULT_FEATURE_VALUE = 0.1;
	private static final double FEATURE_EPSILON = 0.0001;
	private static final double MIN_EPOCHS = 50;
	private static final double TRAINING_BLEND = 0.015; // Funk's K
	private static final double MIN_FEAT_NORM = 0.000001;
	
	private final int featureCount;
	private final double learningRate;
	private final RatingPredictorBuilder baselineBuilder;
	
	@Inject
	public GradientDescentSVDRecommenderBuilder(
			@FeatureCount int features,
			@LearningRate double lrate,
			@Nullable @BaselinePredictor RatingPredictorBuilder baseline) {
		featureCount = features;
		learningRate = lrate;
		baselineBuilder = baseline;
	}

	@Override
	public RecommenderEngine build(RatingDataSource data) {
		logger.debug("Setting up to build SVD recommender with {} features", featureCount);
		
		RatingPredictor baseline;
		if (baselineBuilder != null)
			baseline = baselineBuilder.build(data);
		else
			baseline = new ConstantPredictor(0.0);
		
		Model model = new Model();
		Indexer userIndex = new Indexer();
		model.userIndex = userIndex;
		Indexer itemIndex = new Indexer();
		model.itemIndex = itemIndex;
		List<SVDRating> ratings = indexData(data, baseline, userIndex, itemIndex, model);
		
		logger.debug("Building SVD with {} features", featureCount);
		model.userFeatures = new double[featureCount][userIndex.getObjectCount()];
		model.itemFeatures = new double[featureCount][itemIndex.getObjectCount()];
		for (int i = 0; i < featureCount; i++) {
			trainFeature(model, ratings, i);
		}
		
		logger.debug("Extracting singular values");
		model.singularValues = new double[featureCount];
		for (int feature = 0; feature < featureCount; feature++) {
			double[] ufv = model.userFeatures[feature];
			double ussq = 0;
			int numUsers = model.userIndex.getObjectCount();
			for (int i = 0; i < numUsers; i++) {
				double uf = ufv[i];
				ussq += uf * uf;
			}
			double unrm = (double) Math.sqrt(ussq);
			if (unrm > MIN_FEAT_NORM) {
				for (int i = 0; i < numUsers; i++) {
					ufv[i] /= unrm;
				}
			}
			double[] ifv = model.itemFeatures[feature];
			double issq = 0;
			int numItems = model.itemIndex.getObjectCount();
			for (int i = 0; i < numItems; i++) {
				double fv = ifv[i];
				issq += fv * fv;
			}
			double inrm = (double) Math.sqrt(issq);
			if (inrm > MIN_FEAT_NORM) {
				for (int i = 0; i < numItems; i++) {
					ifv[i] /= inrm;
				}
			}
			model.singularValues[feature] = unrm * inrm;
		}
		
		return new SVDRecommender(featureCount, itemIndex, baseline, model.itemFeatures, model.singularValues);
	}
	
	private void trainFeature(Model model, List<SVDRating> ratings, int feature) {
		double ufv[] = model.userFeatures[feature];
		double ifv[] = model.itemFeatures[feature];
		DoubleArrays.fill(ufv, DEFAULT_FEATURE_VALUE);
		DoubleArrays.fill(ifv, DEFAULT_FEATURE_VALUE);
		
		logger.debug("Training feature {}", feature);
		
		double rmse = Double.MAX_VALUE, oldRmse = 0.0;
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
	
	private List<SVDRating> indexData(RatingDataSource data, RatingPredictor baseline, Indexer userIndex, Indexer itemIndex, Model model) {
		Cursor<Rating> ratings = data.getRatings();
		ArrayList<RatingVector> ratingVectors = new ArrayList<RatingVector>();
		try {
			int nusers = ratings.getRowCount();
			ArrayList<SVDRating> svr = new ArrayList<SVDRating>(nusers >= 0 ? nusers * 5 : 100);
			for (Rating r: ratings) {
				SVDRating svdr = new SVDRating(model, r);
				svr.add(svdr);
				while (svdr.user >= ratingVectors.size()) {
					ratingVectors.add(new RatingVector());
				}
				ratingVectors.get(svdr.user).put(svdr.iid, svdr.value);
			}
			model.userBaselines = new ArrayList<RatingVector>(ratingVectors.size());
			for (int i = 0, sz = ratingVectors.size(); i < sz; i++) {
				RatingVector rv = ratingVectors.get(i);
				long uid = userIndex.getId(i);
				model.userBaselines.add(baseline.predict(uid, rv, rv.idSet()));
			}
			svr.trimToSize();
			return svr;
		} finally {
			ratings.close();
		}
	}
	
	private class Model {
		ArrayList<RatingVector> userBaselines;
		double userFeatures[][];
		double itemFeatures[][];
		double singularValues[];
		Indexer userIndex;
		Indexer itemIndex;
	}
	
	private class SVDRating {
		private final Model model;
		public final long uid, iid;
		public final int user;
		public final int item;
		public final double value;
		private double cachedValue = Double.NaN;
		
		public SVDRating(Model model, Rating r) {
			this.model = model;
			uid = r.getUserId();
			iid = r.getItemId();
			user = model.userIndex.internId(uid);
			item = model.itemIndex.internId(iid);
			this.value = r.getRating();
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
				sum = model.userBaselines.get(user).get(iid);
			else
				sum = cachedValue;
			
			sum += model.itemFeatures[feature][item] * model.userFeatures[feature][user];
			sum += (featureCount - feature - 1) * (DEFAULT_FEATURE_VALUE * DEFAULT_FEATURE_VALUE);
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
}