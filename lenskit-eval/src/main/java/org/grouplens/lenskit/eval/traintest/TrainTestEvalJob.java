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

import com.google.common.base.Supplier;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
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
import org.grouplens.lenskit.eval.SharedPreferenceSnapshot;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.util.io.LKFileUtils;
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
public class TrainTestEvalJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalJob.class);

    private final int numRecs;
    
    @Nonnull
    private final AlgorithmInstance algorithm;
    @Nonnull
    private final List<TestUserMetric> evaluators;
    @Nonnull
    private final TTDataSet data;
    @Nonnull
    private final Supplier<TableWriter> outputSupplier;
    @Nonnull
    private Supplier<TableWriter> userOutputSupplier;
    @Nonnull
    private Supplier<TableWriter> predictOutputSupplier;

    private final Supplier<SharedPreferenceSnapshot> snapshot;
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
     * @param numRecs The number of recommendations to compute.
     */
    public TrainTestEvalJob(AlgorithmInstance algo,
                            List<TestUserMetric> evals,
                            TTDataSet ds, Supplier<SharedPreferenceSnapshot> snap,
                            Supplier<TableWriter> out, int numRecs) {
        algorithm = algo;
        evaluators = evals;
        data = ds;
        snapshot = snap;
        outputSupplier = out;
        this.numRecs = numRecs;
        
        int ncols = 2;
        for (TestUserMetric eval: evals) {
            if (eval.getColumnLabels() != null) {
                ncols += eval.getColumnLabels().length;
            }
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
            Recommender rec = algorithm.buildRecommender(dao, snapshot,
                                                         data.getPreferenceDomain());
            RatingPredictor predictor = rec.getRatingPredictor();
            ItemRecommender recommender = rec.getItemRecommender();

            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Testing {}", algorithm.getName());
            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<TestUserMetricAccumulator> evalAccums = new ArrayList<TestUserMetricAccumulator>(evaluators.size());
            
            DataAccessObject testDao = data.getTestFactory().create();
            try {
                for (TestUserMetric eval: evaluators) {

                    TestUserMetricAccumulator accum =
                        eval.makeAccumulator(algorithm, data);
                    evalAccums.add(accum);
                }

                Cursor<UserHistory<Rating>> userProfiles =
                    testDao.getUserHistories(Rating.class);
                try {
                    for (UserHistory<Rating> p: userProfiles) {
                        long uid = p.getUserId();
                        SparseVector ratings =
                            RatingVectorHistorySummarizer.makeRatingVector(p);

                        Supplier<SparseVector> preds =
                                new PredictionSupplier(predictor, uid, ratings.keySet());
                        Supplier<ScoredLongList> recs =
                                new RecommendationSupplier(recommender, uid, ratings.keySet());
                        Supplier<UserHistory<Rating>> hist = new HistorySupplier(dao, uid);

                        TestUser test = new TestUser(uid, ratings, hist, preds, recs);

                        for (TestUserMetricAccumulator accum: evalAccums) {
                            String[] perUserResults = accum.evaluate(test);
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
                            writePredictions(predictTable, uid, ratings, test.getPredictions());
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
            LKFileUtils.close(userTable, predictTable);
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

    private void writeOutput(StopWatch build, StopWatch test, List<TestUserMetricAccumulator> accums) throws IOException {
        String[] row = new String[outputColumnCount];
        row[0] = Long.toString(build.getTime());
        row[1] = Long.toString(test.getTime());
        int col = 2;
        for (TestUserMetricAccumulator acc: accums) {
            String[] ar = acc.finalResults();
            if (ar != null) {
                // no aggregated output is generated
                int n = ar.length;
                System.arraycopy(ar, 0, row, col, n);
                col += n;
            }
        }
        TableWriter output = outputSupplier.get();
        try {
            output.writeRow(row);
        } finally {
            output.close();
        }
    }

    private class PredictionSupplier implements Supplier<SparseVector> {
        private final RatingPredictor predictor;
        private final long user;
        private final LongSet items;

        public PredictionSupplier(RatingPredictor pred, long id, LongSet is) {
            predictor = pred;
            user = id;
            items = is;
        }

        @Override
        public SparseVector get() {
            if (predictor == null) {
                throw new IllegalArgumentException("cannot compute predictions without a predictor");
            }
            return predictor.score(user, items);
        }
    }

    private class RecommendationSupplier implements Supplier<ScoredLongList> {
        private final ItemRecommender recommender;
        private final long user;
        private final LongSet items;

        public RecommendationSupplier(ItemRecommender rec, long id, LongSet is) {
            recommender = rec;
            user = id;
            items = is;
        }

        @Override
        public ScoredLongList get() {
            if (recommender == null) {
                throw new IllegalArgumentException("cannot compute recommendations without a recommender");
            }
            return recommender.recommend(user, numRecs, items, null);
        }
    }
    
    private class HistorySupplier implements Supplier<UserHistory<Rating>> {
        private final DataAccessObject dao;
        private final long user;
        
        public HistorySupplier(DataAccessObject dao, long id) {
            this.dao = dao;
            user = id;
        }
        
        @Override
        public UserHistory<Rating> get() {
            return dao.getUserHistory(user, Rating.class);
        }
    }
}
