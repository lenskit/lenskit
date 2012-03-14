/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.traintest;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorHistorySummarizer;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.Job;
import org.grouplens.lenskit.eval.SharedRatingSnapshot;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.EvalAccumulator;
import org.grouplens.lenskit.eval.metrics.EvalMetric;
import org.grouplens.lenskit.eval.metrics.predict.PredictEvalAccumulator;
import org.grouplens.lenskit.eval.metrics.recommend.RecommendEvalAccumulator;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;

/**
 * Run a single train-test evaluation of a single algorithm.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TTPredictEvalJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(TTPredictEvalJob.class);

    // FIXME balke: make configurable
    private static final int recSetSize = 5;
    
    @Nonnull
    private final AlgorithmInstance algorithm;
    @Nonnull
    private final List<EvalMetric> evaluators;
    @Nonnull
    private final TTDataSet data;
    @Nonnull
    private final Supplier<TableWriter> outputSupplier;
    @Nonnull
    private Supplier<TableWriter> userOutputSupplier;
    @Nonnull
    private Supplier<TableWriter> predictOutputSupplier;

    private final Supplier<SharedRatingSnapshot> snapshot;
    private final int outputColumnCount;

    /**
     * Create a new train-test eval job.
     * @param algo The algorithm to test.
     * @param evals The evaluators to use.
     * @param ds The data set to use.
     * @param snap Supplier providing access to a shared rating snapshot to use in the
     *             build process.
     * @param out The table writer to receive outputProvider. This writer is expected to
     *        be prefixed with algorithm and group ID data, so only the times
     *        and eval outputProvider needs to be written.
     */
    public TTPredictEvalJob(AlgorithmInstance algo,
                            List<EvalMetric> evals,
                            TTDataSet ds, Supplier<SharedRatingSnapshot> snap,
                            Supplier<TableWriter> out) {
        algorithm = algo;
        evaluators = evals;
        data = ds;
        snapshot = snap;
        outputSupplier = out;
        
        int ncols = 2;
        for (EvalMetric eval: evals) {
            ncols += eval.getColumnLabels().length;
        }
        outputColumnCount = ncols;
    }

    public void setUserOutput(Supplier<TableWriter> out) {
        userOutputSupplier = out;
    }

    /**
     * Set a supplier for the prediction output table. The writer is expected to be
     * prefixed with algorithm and group ID data; the job will only write user, item,
     * rating, and prediction.
     * @param out The table writer supplier.
     */
    public void setPredictOutput(Supplier<TableWriter> out) {
        predictOutputSupplier = out;
    }

    @Override
    public String getName() {
        return algorithm.getName();
    }

    @Override
    public void run() {
        DataAccessObject dao = data.getTrainFactory().snapshot();
        TableWriter userTable = null;
        TableWriter predictTable = null;

        try {
            userTable = userOutputSupplier.get();
            predictTable = predictOutputSupplier.get();


            logger.info("Building {}", algorithm.getName());
            StopWatch buildTimer = new StopWatch();
            buildTimer.start();

            Recommender rec = algorithm.buildRecommender(dao, snapshot.get());
            RatingPredictor predictor = rec.getRatingPredictor();
            ItemRecommender recommender = rec.getItemRecommender();

            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Testing {}", algorithm.getName());
            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<EvalAccumulator> evalAccums =
                    new ArrayList<EvalAccumulator>(evaluators.size());
            
            DataAccessObject testDao = data.getTestFactory().create();
            try {
                for (EvalMetric eval: evaluators) {

                    EvalAccumulator accum =
                        eval.makeAccumulator(algorithm, data);

                    if (accum instanceof PredictEvalAccumulator) {
                        if (predictor != null) {
                            evalAccums.add(accum);
                        } else {
                            logger
                                .error("predict metric configured, but no predictor defined! Skipping metric...");
                        }
                    } else if (accum instanceof RecommendEvalAccumulator) {
                        if (recommender != null) {
                            evalAccums.add(accum);
                        } else {
                            logger
                                .error("recommend metric configured, but no recommender defined! Skipping metric...");
                        }
                    }

                }

                Cursor<UserHistory<Rating>> userProfiles =
                    testDao.getUserHistories(Rating.class);
                try {
                    for (UserHistory<Rating> p: userProfiles) {
                        long uid = p.getUserId();
                        SparseVector ratings =
                            RatingVectorHistorySummarizer.makeRatingVector(p);

                        // check metric type
                        SparseVector predictions = null;
                        ScoredLongList recommendations = null;

                        for (EvalAccumulator accum: evalAccums) {

                            String[] perUserResults = null;
                            if (accum instanceof PredictEvalAccumulator) {
                                if (predictions == null) {
                                    predictions =
                                        predictor.score(p.getUserId(),
                                                        ratings.keySet());
                                }
                                perUserResults =
                                    ((PredictEvalAccumulator) accum)
                                        .evaluatePredictions(uid, ratings,
                                                             predictions);

                            } else if (accum instanceof RecommendEvalAccumulator) {

                                if (recommendations == null) {
                                    recommendations =
                                        recommender
                                            .recommend(p.getUserId(),
                                                       recSetSize,
                                                       ratings.keySet(),
                                                       null);
                                }
                                perUserResults =
                                    ((RecommendEvalAccumulator) accum)
                                        .evaluateRecommendations(p.getUserId(),
                                                                 ratings,
                                                                 recommendations);

                            }

                            if (perUserResults != null && userTable != null) {
                                try {
                                    userTable.writeRow(perUserResults);
                                } catch (IOException e) {
                                    throw new RuntimeException(
                                            "error writing user output", e);
                                }
                            }
                        }

                        if (predictTable != null) {
                            writePredictions(predictTable, uid, ratings,
                                             predictions);
                        }
                    }
                } finally {
                    userProfiles.close();
                }
            } finally {
                testDao.close();
            }
            testTimer.stop();
            logger.info("Tested {} in {}", algorithm.getName(), testTimer);
            
            try {
                writeOutput(buildTimer, testTimer, evalAccums);
            } catch (IOException e) {
                logger.error("Error writing output", e);
            }
        } finally {
            if (userTable != null) {
                Closeables.closeQuietly(userTable);
            }
            if (predictTable != null) {
                Closeables.closeQuietly(predictTable);
            }
            dao.close();
        }
    }

    private void writePredictions(TableWriter predictTable, long uid, SparseVector ratings, SparseVector predictions) {
        String[] row = new String[4];
        row[0] = Long.toString(uid);
        for (Long2DoubleMap.Entry e: ratings.fast()) {
            long iid = e.getLongKey();
            row[1] = Long.toString(iid);
            row[2] = Double.toString(e.getDoubleValue());
            if (predictions.containsKey(iid)) {
                row[3] = Double.toString(predictions.get(iid));
            } else {
                row[3] = null;
            }
            try {
                predictTable.writeRow(row);
            } catch (IOException x) {
                throw new RuntimeException("error writing predictions", x);
            }
        }
    }

    private void writeOutput(StopWatch build, StopWatch test, List<EvalAccumulator> accums) throws IOException {
        String[] row = new String[outputColumnCount];
        row[0] = Long.toString(build.getTime());
        row[1] = Long.toString(test.getTime());
        int col = 2;
        for (EvalAccumulator acc: accums) {
            String[] ar = acc.finalResults();
            int n = ar.length;
            System.arraycopy(ar, 0, row, col, n);
            col += n;
        }
        TableWriter output = outputSupplier.get();
        try {
            output.writeRow(row);
        } finally {
            output.close();
        }
    }
}
