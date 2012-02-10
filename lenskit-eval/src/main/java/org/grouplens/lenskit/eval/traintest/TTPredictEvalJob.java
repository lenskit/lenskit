/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Supplier;
import com.google.common.io.Closeables;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.history.RatingVectorHistorySummarizer;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.Job;
import org.grouplens.lenskit.eval.SharedRatingSnapshot;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.predict.PredictEvalMetric;
import org.grouplens.lenskit.eval.metrics.predict.PredictEvalMetric.Accumulator;
import org.grouplens.lenskit.util.TaskTimer;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Run a single train-test evaluation of a single algorithm.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TTPredictEvalJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(TTPredictEvalJob.class);
    @Nonnull
    private AlgorithmInstance algorithm;
    @Nonnull
    private List<PredictEvalMetric> evaluators;
    @Nonnull
    private TTDataSet data;
    @Nonnull
    private Supplier<TableWriter> outputProvider;
    @Nonnull
    private Supplier<TableWriter> predictOutputSupplier;

    private Supplier<SharedRatingSnapshot> snapshot;
    private int outputColumnCount;

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
                            List<PredictEvalMetric> evals,
                            TTDataSet ds, Supplier<SharedRatingSnapshot> snap,
                            Supplier<TableWriter> out) {
        algorithm = algo;
        evaluators = evals;
        data = ds;
        snapshot = snap;
        outputProvider = out;
        
        int ncols = 2;
        for (PredictEvalMetric eval: evals) {
            ncols += eval.getColumnLabels().length;
        }
        outputColumnCount = ncols;
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
        TableWriter predictTable = null;

        try {
            predictTable = predictOutputSupplier.get();

            logger.info("Building {}", algorithm.getName());
            TaskTimer buildTimer = new TaskTimer();
            Recommender rec = algorithm.buildRecommender(dao, snapshot.get());
            RatingPredictor pred = rec.getRatingPredictor();
            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Testing {}", algorithm.getName());
            TaskTimer testTimer = new TaskTimer();

            List<Accumulator> evalAccums =
                    new ArrayList<Accumulator>(evaluators.size());
            
            DataAccessObject testDao = data.getTestFactory().create();
            try {
                for (PredictEvalMetric eval: evaluators) {
                    evalAccums.add(eval.makeAccumulator(data));
                }
                
                Cursor<UserHistory<Rating>> userProfiles = testDao.getUserHistories(Rating.class);
                try {
                    for (UserHistory<Rating> p: userProfiles) {
                        long uid = p.getUserId();
                        SparseVector ratings = RatingVectorHistorySummarizer.makeRatingVector(p);
                        SparseVector predictions =
                            pred.score(p.getUserId(), ratings.keySet());
                        for (Accumulator accum: evalAccums) {
                            accum.evaluatePredictions(uid, ratings, predictions);
                        }
                        if (predictTable != null) {
                            writePredictions(predictTable, uid, ratings, predictions);
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
                logger.error("Error writing outputProvider", e);
            }
        } finally {
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

    private void writeOutput(TaskTimer build, TaskTimer test, List<Accumulator> accums) throws IOException {
        String[] row = new String[outputColumnCount];
        row[0] = Double.toString(build.elapsed());
        row[1] = Double.toString(test.elapsed());
        int col = 2;
        for (Accumulator acc: accums) {
            String[] ar = acc.results();
            int n = ar.length;
            System.arraycopy(ar, 0, row, col, n);
            col += n;
        }
        TableWriter output = outputProvider.get();
        try {
            output.writeRow(row);
        } finally {
            output.close();
        }
    }
}
