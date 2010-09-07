package org.grouplens.reflens.bench;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.integer.IntRatingVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 * Main class for running k-fold cross-validation benchmarks on recommenders.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class BenchmarkRunner {
	private static Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);
	
	private static void fail(int code, String msg) {
		fail(code, msg, null);
	}
	
	private static void fail(int code, Exception err) {
		fail(code, null, err);
	}
	
	private static void fail(int code, String msg, Exception err) {
		if (msg != null)
			System.err.println(msg);
		if (err != null)
			err.printStackTrace(System.err);
		System.exit(code);
	}

	public static void main(String[] args) {
		BenchmarkOptions options = null;
		try {
			 options = CliFactory.parseArguments(BenchmarkOptions.class, args);
		} catch (ArgumentValidationException e) {
			fail(1, e);
		}
		
		BenchmarkRunner runner = new BenchmarkRunner(options);
		try {
			runner.run();
		} catch (Exception e) {
			fail(2, "Error running recommender benchmark", e);
		}
	}
	
	private BenchmarkOptions options;

	
	private BenchmarkRunner(BenchmarkOptions options) {
		this.options = options;
	}
	
	/**
	 * Load the recommender factory named by className.
	 * @param className The name of the recommender factory class to load.
	 * @return A factory for building recommender engines.
	 */
	private BenchmarkRecommenderFactory getRecommenderFactory(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<BenchmarkRecommenderFactory> factClass =
				(Class<BenchmarkRecommenderFactory>) Class.forName(className);
			Constructor<BenchmarkRecommenderFactory> ctor = factClass.getConstructor();
			return ctor.newInstance();
		} catch (ClassNotFoundException e) {
			fail(1, "Recommender not found", e);
			return null; /* never executed */
		} catch (InstantiationException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Invalid recommender fatory", e);
		}
	}
	
	private void run() {
		BenchmarkRecommenderFactory factory = ObjectLoader.makeInstance(options.getRecEngine());
		RatingSet<Integer,Integer> data = null;
		try {
			data = new RatingSet<Integer,Integer>(
					options.getNumFolds(), readRatingsData());
		} catch (FileNotFoundException e) {
			fail(3, e);
		}
		
		// We now have data and a factory. Let's go to town!
		BenchmarkAggregator agg = new BenchmarkAggregator(factory);
		agg.setHoldoutFraction(options.getHoldoutFraction());
		for (int i = 0; i < data.getChunkCount(); i++) {
			logger.info(String.format("Running benchmark set %d", i));
			agg.addBenchmark(data.trainingSet(i), data.testSet(i));
		}
		agg.printResults();
	}
	
	private List<RatingVector<Integer,Integer>> readRatingsData() throws FileNotFoundException {
		Int2ObjectMap<RatingVector<Integer,Integer>> users =
			new Int2ObjectOpenHashMap<RatingVector<Integer,Integer>>();
		Pattern splitter = Pattern.compile(Pattern.quote(options.getDelimiter()));
		for (String file: options.getFiles()) {
			Scanner s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				String line = s.nextLine();
				String[] fields = splitter.split(line);
				if (fields.length < 3) {
					fail(3, "invalid input line");
				}
				int uid = Integer.parseInt(fields[0]);
				int iid = Integer.parseInt(fields[1]);
				float rating = Float.parseFloat(fields[2]);
				RatingVector<Integer,Integer> history = null;
				if (users.containsKey(uid)) {
					history = users.get(uid);
				} else {
					history = new IntRatingVector(uid);
					users.put(uid, history);
				}
				history.putRating(iid, rating);
			}
			s.close();
		}
		return new ArrayList<RatingVector<Integer,Integer>>(users.values());
	}
}
