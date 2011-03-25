/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.eval.crossfold;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderNotAvailableException;
import org.grouplens.lenskit.RecommenderService;
import org.grouplens.lenskit.data.UserRatingProfile;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.CrossfoldOptions;
import org.grouplens.lenskit.eval.TaskTimer;
import org.grouplens.lenskit.tablewriter.CSVWriterBuilder;
import org.grouplens.lenskit.tablewriter.TableWriter;
import org.grouplens.lenskit.tablewriter.TableWriterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run a crossfold benchmark.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CrossfoldEvaluator implements Runnable {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossfoldEvaluator.class);
    private final CrossfoldManager manager;
    private final int numFolds;
    private final UserRatingProfileSplitter profileSplitter;
    private final List<AlgorithmInstance> algorithms;

    private TableWriter writer, predWriter;
    private int colRunNumber, colTestSize, colTrainSize, colAlgo, colMAE, colRMSE;
    private int colNTry, colNGood, colCoverage;
    private int colBuildTime;
    private int colPredTime;
    
    public CrossfoldEvaluator(RatingDataAccessObject ratings,
            List<AlgorithmInstance> algorithms,
            int numFolds,
            UserRatingProfileSplitter splitter,
            Writer output) throws IOException {
        this.numFolds = numFolds;
        this.algorithms = algorithms;
        writer = makeWriter(output);
        profileSplitter = splitter;
        manager = new CrossfoldManager(numFolds, ratings);
    }

    public CrossfoldEvaluator(RatingDataAccessObject ratings, CrossfoldOptions options,
            List<AlgorithmInstance> algorithms, Writer output) throws IOException {
        this(ratings, algorithms, options.getNumFolds(),
                (options.timeSplit() ? new TimestampUserRatingProfileSplitter(options.getHoldoutFraction())
                    : new RandomUserRatingProfileSplitter(options.getHoldoutFraction())),
                output);

        if (!options.predictionFile().isEmpty()) {
            logger.info("Writing predictions to {}", options.predictionFile());
            TableWriterBuilder builder = new CSVWriterBuilder();
            builder.addColumn("Fold");
            builder.addColumn("Algorithm");
            builder.addColumn("User");
            builder.addColumn("Item");
            builder.addColumn("Rating");
            builder.addColumn("Prediction");
            predWriter = builder.makeWriter(new FileWriter(options.predictionFile()));
        }
    }

    public void run() {
        for (int i = 0; i < numFolds; i++) {
            RatingDataAccessObject train = manager.trainingSet(i);
            try {
                Collection<UserRatingProfile> test = manager.testSet(i);
                int nusers = train.getUserCount();
                logger.info(String.format("Running benchmark %d with %d training and %d test users",
                        i+1, nusers, test.size()));
                for (AlgorithmInstance algo: algorithms) {
                    writer.setValue(colRunNumber, i+1);
                    writer.setValue(colTrainSize, nusers);
                    writer.setValue(colTestSize, test.size());
                    writer.setValue(colAlgo, algo.getName());
                    benchmarkAlgorithm(i+1, algo, train, test);
                    writer.finishRow();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            writer.finish();
            if (predWriter != null)
                predWriter.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Print the header for the data file
     * @param algorithms
     * @throws IOException
     */
    private TableWriter makeWriter(Writer output) throws IOException {
        TableWriterBuilder bld = new CSVWriterBuilder();
        colRunNumber = bld.addColumn("RunNumber");
        colTrainSize = bld.addColumn("TrainSize");
        colTestSize = bld.addColumn("TestSize");
        colAlgo = bld.addColumn("Algorithm");
        colMAE = bld.addColumn("MAE");
        colRMSE = bld.addColumn("RMSE");
        colNTry = bld.addColumn("NTried");
        colNGood = bld.addColumn("NGood");
        colCoverage = bld.addColumn("Coverage");
        colBuildTime = bld.addColumn("BuildTime");
        colPredTime = bld.addColumn("PredTime");

        return bld.makeWriter(output);
    }

    private void benchmarkAlgorithm(int runNumber, AlgorithmInstance algo, RatingDataAccessObject train, Collection<UserRatingProfile> test) {
        TaskTimer timer = new TaskTimer();
        logger.debug("Benchmarking {}", algo.getName());
        RecommenderService engine;
        logger.debug("Building recommender");
        try {
            engine = algo.getRecommenderService(train);
        } catch (RecommenderNotAvailableException e) {
            logger.error("Recommender not available: {}", e);
            throw new RuntimeException(e);
        }
        RatingPredictor rec = engine.getRatingPredictor();
        writer.setValue(colBuildTime, timer.elapsed());
        logger.debug("Built model {} model in {}",
                algo.getName(), timer.elapsedPretty());

        logger.debug("Testing recommender");
        TaskTimer testTimer = new TaskTimer();
        double accumErr = 0.0f;		// accmulated error
        double accumSqErr = 0.0f;	// accumluated squared error
        int nitems = 0;				// total ratings
        int ngood = 0;				// total predictable ratings
        for (UserRatingProfile user: test) {
            final long uid = user.getUser();
            SplitUserRatingProfile split = profileSplitter.splitProfile(user);          
            
            // Compute predictions
            final SparseVector predictions = rec.predict(uid, split.getQueryVector(),
            		split.getProbeVector().keySet());
            
            // Evaluate predictions
            for (final Long2DoubleMap.Entry entry: split.getProbeVector().fast()) {
                final long iid = entry.getLongKey();
                final double rating = entry.getDoubleValue();
                final double prediction = predictions.get(iid);
                nitems++;
                if (predWriter != null) {
                    try {
                        predWriter.writeRow(runNumber, algo.getName(),
                                uid, iid, rating,
                                Double.isNaN(prediction) ? null : prediction);
                    } catch (IOException e) {
                        logger.error("Error writing to pred. table: {}", e);
                        predWriter = null;
                    }
                }
                if (!Double.isNaN(prediction)) {
                    double err = prediction - rating;
                    ngood++;
                    accumErr += Math.abs(err);
                    accumSqErr += err * err;
                }
            }
        }
        double mae = accumErr / ngood;
        double rmse = accumSqErr / ngood;
        double cov = (double) ngood / nitems;
        logger.info(String.format("Recommender %s finished in %s (cov=%f, mae=%f, rmse=%f)",
                algo.getName(), timer.elapsedPretty(), cov, mae, rmse));
        writer.setValue(colPredTime, testTimer.elapsed());
        writer.setValue(colMAE, mae);
        writer.setValue(colRMSE, rmse);
        writer.setValue(colNTry, nitems);
        writer.setValue(colNGood, ngood);
        writer.setValue(colCoverage, cov);
    }
}
