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

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.data.BasicUserRatingProfile;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.data.integer.IntDataModule;
import org.grouplens.reflens.util.ObjectLoader;
import org.grouplens.reflens.util.ProgressReporterFactory;
import org.grouplens.reflens.util.TerminalProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.util.Providers;

/**
 * Main class for running k-fold cross-validation benchmarks on recommenders.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public final class BenchmarkRunner {
	private static Logger logger = LoggerFactory
			.getLogger(BenchmarkRunner.class);

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
			System.err.println(e.getMessage());
			System.exit(1);
		}

		BenchmarkRunner runner = new BenchmarkRunner(options);
		try {
			runner.run();
		} catch (Exception e) {
			fail(2, "Error running recommender benchmark", e);
		}
	}

	private BenchmarkOptions options;
	private Injector injector;

	private BenchmarkRunner(BenchmarkOptions options) {
		this.options = options;
	}

	private void run() {
		String moduleName = options.getModule();
		logger.debug("Loading module {}", moduleName);
		Module recModule = ObjectLoader.makeInstance(moduleName);
		injector = Guice.createInjector(new AbstractModule() {
			protected void configure() {
				if (options.showProgress()) {
					bind(ProgressReporterFactory.class).toProvider(
							FactoryProvider.newFactory(ProgressReporterFactory.class,
									TerminalProgressReporter.class));
				} else {
					bind(ProgressReporterFactory.class).toProvider((Provider) Providers.of(null));
				}
			}
		}, new IntDataModule(), recModule);
		RatingSet<Integer, Integer> data = null;
		try {
			logger.debug("Loading ratings data");
			data = new RatingSet<Integer, Integer>(options.getNumFolds(),
					readRatingsData());
		} catch (FileNotFoundException e) {
			fail(3, e);
		}

		RecommenderBuilder<Integer, Integer> factory = null;
		try {
			factory = injector
					.getInstance(Key
							.get(new TypeLiteral<RecommenderBuilder<Integer, Integer>>() {
							}));
		} catch (CreationException e) {
			fail(2, e.getMessage());
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

	private List<UserRatingProfile<Integer, Integer>> readRatingsData()
			throws FileNotFoundException {
		Int2ObjectMap<Int2DoubleMap> users = new Int2ObjectOpenHashMap<Int2DoubleMap>();
		Pattern splitter = Pattern.compile(Pattern
				.quote(options.getDelimiter()));
		for (String file : options.getFiles()) {
			Scanner s = new Scanner(new File(file));
			while (s.hasNextLine()) {
				String line = s.nextLine();
				String[] fields = splitter.split(line);
				if (fields.length < 3) {
					fail(3, "invalid input line");
				}
				int uid = Integer.parseInt(fields[0]);
				int iid = Integer.parseInt(fields[1]);
				double rating = Double.parseDouble(fields[2]);
				Int2DoubleMap history = null;
				if (users.containsKey(uid)) {
					history = users.get(uid);
				} else {
					history = new Int2DoubleOpenHashMap();
					users.put(uid, history);
				}
				history.put(iid, rating);
			}
			s.close();
		}
		List<UserRatingProfile<Integer, Integer>> profiles =
			new ArrayList<UserRatingProfile<Integer,Integer>>();
		for (Map.Entry<Integer, Int2DoubleMap> user: users.entrySet()) {
			UserRatingProfile<Integer,Integer> p =
				new BasicUserRatingProfile<Integer, Integer>(user);
			profiles.add(p);
		}
		return profiles;
	}
}
