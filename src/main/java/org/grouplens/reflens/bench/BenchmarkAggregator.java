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

package org.grouplens.reflens.bench;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collection;
import java.util.Collections;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RecommendationEngine;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.integer.IntRatingVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to aggregate benchmarking runs.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BenchmarkAggregator {
	private static Logger logger = LoggerFactory.getLogger(BenchmarkAggregator.class);
	private RecommenderBuilder<Integer,Integer> factory;
	private int numRuns = 0;
	private float tMAE = 0.0f;
	private float tRMSE = 0.0f;
	private float tCov = 0.0f;
	private double holdout = 0.33333333;
	
	public BenchmarkAggregator(RecommenderBuilder<Integer,Integer> factory) {
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
			Collection<RatingVector<Integer,Integer>> trainUsers,
			Collection<RatingVector<Integer,Integer>> testUsers) {
		logger.debug("Building model with {} users", trainUsers.size());
		RecommendationEngine<Integer, Integer> engine = factory.build(trainUsers);
		RatingPredictor<Integer, Integer> rec = engine.getRatingPredictor();
		
		logger.debug("Testing model with {} users", testUsers.size());
		float accumErr = 0.0f;
		float accumSqErr = 0.0f;
		int nitems = 0;
		int ngood = 0;
		for (RatingVector<Integer,Integer> user: testUsers) {
			IntArrayList ratedItems = new IntArrayList(user.getRatings().keySet());
			int midpt = (int) Math.round(ratedItems.size() * (1.0 - holdout));
			// TODO: make this support timestamped ratings
			Collections.shuffle(ratedItems);
			IntRatingVector<Integer> partialHistory = new IntRatingVector<Integer>(user.getOwner());
			for (int i = 0; i < midpt; i++) {
				int iid = ratedItems.getInt(i);
				partialHistory.putRating(iid, user.getRating(iid));
			}
			for (int i = midpt; i < ratedItems.size(); i++) {
				int iid = ratedItems.getInt(i);
				ObjectValue<Integer> prediction = rec.predict(partialHistory, iid);
				nitems++;
				if (prediction != null) {
					float err = prediction.getRating() - user.getRating(iid);
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
		tCov += (float) ngood / nitems;
	}
	
	public void printResults() {
		System.out.format("Ran %d folds.\n", numRuns);
		System.out.format("Average MAE: %f\n", tMAE / numRuns);
		System.out.format("Average RMSE: %f\n", tRMSE / numRuns);
		System.out.format("Average coverage: %f\n", tCov / numRuns);
	}
}
