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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.grouplens.reflens.data.RatingDataSource;
import org.grouplens.reflens.data.SimpleFileDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 * Main class for running k-fold cross-validation benchmarks on recommenders.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public final class BenchmarkRunner {
	private static Logger logger = LoggerFactory
			.getLogger(BenchmarkRunner.class);
	
	@SuppressWarnings("serial")
	private static class AbortException extends RuntimeException {
		private int code;
		public AbortException(int code) {
			super();
			this.code = code;
		}
		
		public int getCode() {
			return code;
		}
	}

	/**
	 * Abort the program with an error message and exit code.
	 * @see #fail(int, String, Exception)
	 * @param code The exit code.
	 * @param msg The message (will be printed on {@link System#err}).
	 */
	@SuppressWarnings("unused")
	private static void fail(int code, String msg) {
		// look for a no-return annotation for this method
		fail(code, msg, null);
	}

	/**
	 * Abort the program with an exception stack trace.
	 * @see #fail(int, String, Exception)
	 * @param code The exit code.
	 * @param err The exception to stacktrace.
	 */
	@SuppressWarnings("unused")
	private static void fail(int code, Exception err) {
		fail(code, null, err);
	}

	/**
	 * Abort the program with an error message, possibly augmented with an
	 * exception.
	 * @param code The exit code.
	 * @param msg The error message (can be <tt>null</tt>; otherwise, will be
	 * printed to {@link System#err})
	 * @param err The exception (if not <tt>null</tt>, its stack trace will be
	 * printed to {@link System#err})
	 */
	private static void fail(int code, String msg, Exception err) {
		logger.error("Aborting with code {}: {}", code, msg);
		if (msg != null)
			System.err.println(msg);
		if (err != null)
			err.printStackTrace(System.err);
		throw new AbortException(code);
	}

	/**
	 * Entry point for the benchmark runner.  This just parses the command line
	 * arguments, creates a benchmark runner, and gets it going.
	 * @param args The command line arguments.
	 */
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
		} catch (AbortException e) {
			System.exit(e.getCode());
		}
	}
	
	/* The actual BenchmarkRunner implementation starts here. */

	private BenchmarkOptions options;

	/**
	 * Instantiate a new benchmark runner with options.
	 * @param options The options passed to the benchmark runner.
	 */
	private BenchmarkRunner(BenchmarkOptions options) {
		this.options = options;
	}

	/**
	 * Do the real work of running the benchmarks.
	 */
	private void run() {
		List<AlgorithmInstance> algos = loadAlgorithms();
		
		PrintStream output = System.out;
		File outFile = options.getOutputFile();
		if (!outFile.getName().isEmpty()) {
			try {
				output = new PrintStream(outFile);
			} catch (FileNotFoundException e) {
				fail(2, "Error opening output file", e);
			}
		}
		
		RatingDataSource data;
		try {
			data = new SimpleFileDataSource(options.getInputFile(), options.getDelimiter());
		} catch (FileNotFoundException e) {
			fail(2, "Error loading input data", e);
			return; /* fail will not return */
		}

		try {
			CrossfoldBenchmark benchmark = new CrossfoldBenchmark(output, data, options.getNumFolds(), options.getHoldoutFraction());
			benchmark.run(algos);
		} catch (Exception e) {
			fail(3, "Error running benchmark", e);
		} finally {
			output.close();
		}
	}
	
	List<AlgorithmInstance> loadAlgorithms() {
		List<AlgorithmInstance> algos = new ArrayList<AlgorithmInstance>();
		for (File f: options.getRecommenderSpecs()) {
			try {
				algos.add(new AlgorithmInstance(f, null));
			} catch (InvalidRecommenderException e) {
				fail(2, "Error loading specification " + f.getName(), e);
			}
		}
		return algos;
	}
}
