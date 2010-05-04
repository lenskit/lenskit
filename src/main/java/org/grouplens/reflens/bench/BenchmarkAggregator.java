package org.grouplens.reflens.bench;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.grouplens.reflens.Recommender;
import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.integer.IntRatingVector;

/**
 * Class to aggregate benchmarking runs.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class BenchmarkAggregator {
	private static Logger logger = Logger.getLogger(BenchmarkAggregator.class.getName());
	private RecommenderFactory factory;
	private int numRuns = 0;
	private float tMAE = 0.0f;
	private float tRMSE = 0.0f;
	private float tCov = 0.0f;
	
	public BenchmarkAggregator(RecommenderFactory factory) {
		this.factory = factory;
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
		logger.info(String.format("Building model with %d users", trainUsers.size()));
		Recommender<Integer, Integer> model = factory.buildRecommender(trainUsers);
		
		logger.info(String.format("Testing model with %d users", testUsers.size()));
		float accumErr = 0.0f;
		float accumSqErr = 0.0f;
		int nitems = 0;
		int ngood = 0;
		for (RatingVector<Integer,Integer> user: testUsers) {
			IntArrayList ratedItems = new IntArrayList(user.getRatings().keySet());
			int midpt = ratedItems.size() * 2 / 3;
			// TODO: make this support timestamped ratings
			Collections.shuffle(ratedItems);
			IntRatingVector partialHistory = new IntRatingVector(user.getOwner());
			for (int i = 0; i < midpt; i++) {
				int iid = ratedItems.getInt(i);
				partialHistory.putRating(iid, user.getRating(iid));
			}
			for (int i = midpt; i < ratedItems.size(); i++) {
				int iid = ratedItems.getInt(i);
				ObjectValue<Integer> prediction = model.predict(partialHistory, iid);
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
