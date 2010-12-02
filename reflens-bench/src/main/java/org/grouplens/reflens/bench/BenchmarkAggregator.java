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

package org.grouplens.reflens.bench;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RecommendationEngine;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.UserRatingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to aggregate benchmarking runs.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BenchmarkAggregator {
	private static Logger logger = LoggerFactory.getLogger(BenchmarkAggregator.class);
	private RecommenderBuilder factory;
	private int numRuns = 0;
	private double tMAE = 0.0f;
	private double tRMSE = 0.0f;
	private double tCov = 0.0f;
	private double holdout = 0.33333333;
	
	public BenchmarkAggregator(RecommenderBuilder factory) {
		this.factory = factory;
	}
	
	public double holdoutFraction() {
		return holdout;
	}
	public void setHoldoutFraction(double fraction) {
		if (fraction <= 0 || fraction >= 1) {
			throw new RuntimeException("Invalid holdout fraction");
		}
		holdout = fraction;
	}
	
	/**
	 * Run the benchmarker over a train/test set and record the results.
	 * @param trainUsers The set of users for building the model.
	 * @param testUsers The set of users for testing the resulting model.  Users
	 * are tested by holding back 1/3 of their ratings.
	 */
	public void addBenchmark(
			RatingDataSource trainingData,
			Collection<UserRatingProfile> testUsers) {
		RecommendationEngine engine;
		logger.debug("Building model with {} users, {} items", trainingData.getUserCount(),
				trainingData.getItemCount());
		try {
			engine = factory.build(trainingData);
		} finally {
			trainingData.close();
		}
		RatingPredictor rec = engine.getRatingPredictor();
		
		logger.debug("Testing model with {} users", testUsers.size());
		double accumErr = 0.0f;
		double accumSqErr = 0.0f;
		int nitems = 0;
		int ngood = 0;
		for (UserRatingProfile user: testUsers) {
			List<Rating> ratings = new ArrayList<Rating>(user.getRatings());
			int midpt = (int) Math.round(ratings.size() * (1.0 - holdout));
			// TODO make this support timestamped ratings
			Collections.shuffle(ratings);
			Long2DoubleMap queryRatings = new Long2DoubleOpenHashMap();
			for (int i = 0; i < midpt; i++) {
				Rating rating = ratings.get(i);
				queryRatings.put(rating.getItemId(), rating.getRating());
			}
			for (int i = midpt; i < ratings.size(); i++) {
				long iid = ratings.get(i).getItemId();
				ScoredId prediction = rec.predict(user.getUser(), queryRatings, iid);
				nitems++;
				if (prediction != null) {
					double err = prediction.getScore() - user.getRating(iid);
					ngood++;
					accumErr += Math.abs(err);
					accumSqErr += err * err;
				}
			}
		}
		
		System.out.format("Finished run. MAE=%f, RMSE=%f, coverage=%d/%d\n",
				accumErr / ngood, Math.sqrt(accumSqErr / ngood), ngood, nitems);
		numRuns++;
		tMAE += accumErr / ngood;
		tRMSE += Math.sqrt(accumSqErr / ngood);
		tCov += (double) ngood / nitems;
	}
	
	public void printResults() {
		System.out.format("Ran %d folds.\n", numRuns);
		System.out.format("Average MAE: %f\n", tMAE / numRuns);
		System.out.format("Average RMSE: %f\n", tRMSE / numRuns);
		System.out.format("Average coverage: %f\n", tCov / numRuns);
	}
}
