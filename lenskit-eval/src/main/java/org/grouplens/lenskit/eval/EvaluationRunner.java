/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
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
package org.grouplens.lenskit.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.RatingCollectionDataSource;
import org.grouplens.lenskit.data.RatingDataSource;
import org.grouplens.lenskit.data.SimpleFileDataSource;
import org.grouplens.lenskit.eval.crossfold.CrossfoldEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

/**
 * Main class for running k-fold cross-validation benchmarks on recommenders.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class EvaluationRunner {
    private static Logger logger = LoggerFactory
            .getLogger(EvaluationRunner.class);

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
        EvaluatorOptions options = null;
        try {
            options = CliFactory.parseArguments(EvaluatorOptions.class, args);
        } catch (ArgumentValidationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        EvaluationRunner runner = new EvaluationRunner(options);
        try {
            runner.run();
        } catch (AbortException e) {
            System.exit(e.getCode());
        }
    }

    /* The actual EvaluationRunner implementation starts here. */

    private EvaluatorOptions options;

    /**
     * Instantiate a new benchmark runner with options.
     * @param options The options passed to the benchmark runner.
     */
    private EvaluationRunner(EvaluatorOptions options) {
        this.options = options;
    }

    /**
     * Do the real work of running the benchmarks.
     */
    private void run() {
        List<AlgorithmInstance> algos = loadAlgorithms();

        if (options.getGraphMode()) {
            File outFile = options.getOutputFile();
            logger.info("Writing graph to file {}", outFile);
            PrintWriter output = new PrintWriter(System.out);
            try {
                if (!outFile.getName().isEmpty()) {
                    try {
                        output = new PrintWriter(outFile);
                    } catch (FileNotFoundException e) {
                        fail(2, "Error opening output file", e);
                    }
                }
                Injector graphInjector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
                GraphvizRenderer renderer = graphInjector.getInstance(GraphvizRenderer.class);
                renderer.setOut(output).setRankdir("TB");
                Provider<InjectorGrapher> gprovider = graphInjector.getProvider(InjectorGrapher.class);
                for (AlgorithmInstance algo: algos) {
                    Injector injector = Guice.createInjector(new AbstractModule() {
                        protected void configure() {
                        }
                        @SuppressWarnings("unused")
                        @Provides public RatingDataSource provideDataSource() {
                            throw new RuntimeException("No data source available");
                        }
                    }, algo.getModule());
                    try {
                        gprovider.get().of(injector).graph();
                    } catch (IOException e) {
                        fail(3, "Error graphing injector", e);
                    }
                }
            } finally {
                if (outFile.getName().isEmpty())
                    output.close();
            }
            logger.info("Graph written, shutting down");
            return;
        }

        RatingDataSource data = null;
        try {
            data = new SimpleFileDataSource(options.getInputFile(), options.getDelimiter());
            if (options.preloadData()) {
                RatingDataSource source = data;
                Cursor<Rating> ratings = null;
                try {
                    ratings = source.getRatings();
                    data = new RatingCollectionDataSource(Cursors.makeList(ratings));
                } finally {
                    if (ratings != null)
                        ratings.close();
                    source.close();
                }
            }

            Writer output = new OutputStreamWriter(System.out);
            File outFile = options.getOutputFile();
            if (!outFile.getName().isEmpty()) {
                try {
                    output = new FileWriter(outFile);
                } catch (IOException e) {
                    fail(2, "Error opening output file", e);
                }
            }

            try {
                CrossfoldEvaluator benchmark = new CrossfoldEvaluator(data, options, algos, output);
                benchmark.run();
            } catch (Exception e) {
                fail(3, "Error running benchmark", e);
            }
        } catch (FileNotFoundException e) {
            fail(2, "Error loading input data", e);
            return; /* fail will not return */
        } finally {
            if (data != null)
                data.close();
        }


    }

    List<AlgorithmInstance> loadAlgorithms() {
        List<AlgorithmInstance> algos = new ArrayList<AlgorithmInstance>();
        for (File f: options.getRecommenderSpecs()) {
            try {
                algos.add(AlgorithmInstance.load(f));
            } catch (InvalidRecommenderException e) {
                fail(2, "Error loading specification " + f.getName(), e);
            }
        }
        return algos;
    }
}
