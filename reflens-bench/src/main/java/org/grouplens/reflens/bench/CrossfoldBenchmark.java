package org.grouplens.reflens.bench;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.RecommenderEngine;
import org.grouplens.reflens.bench.crossfold.CrossfoldManager;
import org.grouplens.reflens.data.Rating;
import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.UserRatingProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a crossfold benchmark.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CrossfoldBenchmark {
	private static final Logger logger = LoggerFactory.getLogger(CrossfoldBenchmark.class);
	private final PrintStream out;
	private final CrossfoldManager manager;
	private final int numFolds;
	private final double holdoutFraction;
	private final boolean wideOutput;
	
	public CrossfoldBenchmark(PrintStream output, RatingDataSource ratings, CrossfoldOptions options) {
		out = output;
		numFolds = options.getNumFolds();
		holdoutFraction = options.getHoldoutFraction();
		wideOutput = options.useWideOutput();
		manager = new CrossfoldManager(numFolds, ratings);
	}
	
	public void run(List<AlgorithmInstance> algorithms) {
		printHeader(algorithms);
		
		for (int i = 0; i < numFolds; i++) {
			RatingDataSource train = manager.trainingSet(i);
			try {
				Collection<UserRatingProfile> test = manager.testSet(i);
				int nusers = train.getUserCount();
				logger.info(String.format("Running benchmark %d with %d training and %d test users",
						i+1, nusers, test.size()));
				if (wideOutput)
					out.format("%d,%d", nusers, test.size());

				for (AlgorithmInstance algo: algorithms) {
					if (!wideOutput)
						out.format("%d,%d", nusers, test.size());
					benchmarkAlgorithm(algo, train, test);
				}
				if (wideOutput)
					out.println();
			} finally {
				train.close();
			}
		}
	}

	/**
	 * Print the header for the data file
	 * @param algorithms
	 */
	private void printHeader(List<AlgorithmInstance> algorithms) {
		if (wideOutput) {
			out.print("TrainSize,TestSize");
			for (AlgorithmInstance a: algorithms) {
				out.format(",\"%s.mae\",\"%s.rmse\",\"%s.cov\"", a.getName(), a.getName(), a.getName());
			}
			out.println();
		} else {
			out.println("TrainSize,TestSize,Algorithm,MAE,RMSE,NTried,NGood,Coverage");
		}
	}
	
	private void benchmarkAlgorithm(AlgorithmInstance algo, RatingDataSource train, Collection<UserRatingProfile> test) {
		TaskTimer timer = new TaskTimer();
		logger.debug("Benchmarking {}", algo.getName());
		RecommenderEngine engine;
		logger.debug("Building recommender");
		engine = algo.getBuilder().build(train);
		RatingPredictor rec = engine.getRatingPredictor();
		logger.debug("Built model {} model in {}",
				algo.getName(), timer.elapsedPretty());
		
		logger.debug("Testing recommender");
		double accumErr = 0.0f;		// accmulated error
		double accumSqErr = 0.0f;	// accumluated squared error
		int nitems = 0;				// total ratings
		int ngood = 0;				// total predictable ratings
		for (UserRatingProfile user: test) {
			List<Rating> ratings = new ArrayList<Rating>(user.getRatings());
			int midpt = (int) Math.round(ratings.size() * (1.0 - holdoutFraction));
			Collections.shuffle(ratings);
			RatingVector queryRatings = new RatingVector();
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
		double mae = accumErr / ngood;
		double rmse = accumSqErr / ngood;
		double cov = (double) nitems / ngood;
		logger.info(String.format("Recommender %s finished in %s (mae=%f, rmse=%f)",
				algo.getName(), timer.elapsedPretty(), mae, rmse));
		if (wideOutput)
			out.format(",%f,%f,%f", mae, rmse, cov);
		else
			out.format("%s,%f,%f,%d,%d,%f\n", algo.getName(), mae, rmse, nitems, ngood, cov);
	}
}
