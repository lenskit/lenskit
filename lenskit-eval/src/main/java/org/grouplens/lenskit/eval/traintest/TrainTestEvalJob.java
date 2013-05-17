/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.Job;
import org.grouplens.lenskit.eval.SharedPreferenceSnapshot;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.RecommenderInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Run a single train-test evaluation of a single algorithm.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public class TrainTestEvalJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalJob.class);

    private final int numRecs;

    @Nonnull
    private final AlgorithmInstance algorithm;
    @Nonnull
    private final List<TestUserMetric> evaluators;
    @Nonnull
    private final List<ModelMetric> modelMetrics;
    @Nonnull
    private final List<Pair<Symbol, String>> channels;
    @Nonnull
    private final TTDataSet data;
    @Nonnull
    private final Supplier<TableWriter> outputSupplier;
    @Nonnull
    private final Supplier<TableWriter> userOutputSupplier;
    @Nonnull
    private final Supplier<TableWriter> predictOutputSupplier;
    private final Supplier<SharedPreferenceSnapshot> snapshot;

    /**
     * Create a new train-test eval job.
     *
     * @param algo    The algorithm to test.
     * @param evals   The evaluators to use.
     * @param chans   The list of channels to extract.
     * @param ds      The data set to use.
     * @param snap    Supplier providing access to a shared rating snapshot to use in the
     *                build process.
     * @param out     The table writer to receive output. This writer is expected to
     *                be prefixed with algorithm and group ID data, so only the times
     *                and eval outputProvider needs to be written.
     * @param userOut The table writer to receive user output. The supplier may return
     *                {@code null}.
     * @param predOut The table writer to receive prediction output. The supplier may
     *                return {@code null}.
     * @param nrecs The number of recommendations to compute.
     */
    public TrainTestEvalJob(@Nonnull AlgorithmInstance algo,
                            @Nonnull List<TestUserMetric> evals,
                            @Nonnull List<ModelMetric> mMetrics,
                            @Nonnull List<Pair<Symbol,String>> chans,
                            @Nonnull TTDataSet ds, Supplier<SharedPreferenceSnapshot> snap,
                            @Nonnull Supplier<TableWriter> out,
                            @Nonnull Supplier<TableWriter> userOut,
                            @Nonnull Supplier<TableWriter> predOut,
                            int nrecs) {
        algorithm = algo;
        evaluators = evals;
        modelMetrics = mMetrics;
        channels = chans;
        data = ds;
        snapshot = snap;
        outputSupplier = out;
        userOutputSupplier = userOut;
        predictOutputSupplier = predOut;
        numRecs = nrecs;
    }

    @Override
    public String getName() {
        return algorithm.getName();
    }

    @Override
    public String getDescription() {
        return algorithm.toString() + " on " + data.getName();
    }

    @Override
    public void run() {
        TableWriter userTable = null;
        TableWriter predictTable = null;

        try {
            userTable = userOutputSupplier.get();
            predictTable = predictOutputSupplier.get();

            List<Object> outputRow = Lists.newArrayList();

            ExecutionInfo execInfo = buildExecInfo();

            logger.info("Building {}", algorithm.getName());
            StopWatch buildTimer = new StopWatch();
            buildTimer.start();
            RecommenderInstance rec = algorithm.makeTestableRecommender(data, snapshot, execInfo);
            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Measuring {}", algorithm.getName());
            for (ModelMetric metric: modelMetrics) {
                outputRow.addAll(metric.measureAlgorithm(algorithm, data, rec.getRecommender()));
            }

            logger.info("Testing {}", algorithm.getName());
            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<TestUserMetricAccumulator> evalAccums = new ArrayList<TestUserMetricAccumulator>(evaluators.size());

            List<Object> userRow = new ArrayList<Object>();

            DataAccessObject testDao = data.getTestFactory().create();
            try {
                for (TestUserMetric eval : evaluators) {

                    TestUserMetricAccumulator accum =
                            eval.makeAccumulator(algorithm, data);
                    evalAccums.add(accum);
                }

                Cursor<UserHistory<Event>> userProfiles = testDao.getUserHistories();
                try {
                    for (UserHistory<Event> p : userProfiles) {
                        assert userRow.isEmpty();
                        userRow.add(p.getUserId());

                        long uid = p.getUserId();
                        LongSet testItems = p.itemSet();

                        Supplier<SparseVector> preds =
                                new PredictionSupplier(rec, uid, testItems);
                        Supplier<ScoredLongList> recs =
                                new RecommendationSupplier(rec, uid, testItems);
                        Supplier<UserHistory<Event>> hist = new HistorySupplier(rec.getDAO(), uid);
                        Supplier<UserHistory<Event>> testHist = Suppliers.ofInstance(p);

                        TestUser test = new TestUser(uid, hist, testHist, preds, recs);

                        for (TestUserMetricAccumulator accum : evalAccums) {
                            Object[] ures = accum.evaluate(test);
                            if (ures != null) {
                                userRow.addAll(Arrays.asList(ures));
                            }
                        }
                        if (userTable != null) {
                            try {
                                userTable.writeRow(userRow);
                            } catch (IOException e) {
                                throw new RuntimeException("error writing user row", e);
                            }
                        }
                        userRow.clear();

                        if (predictTable != null) {
                            writePredictions(predictTable, uid,
                                             RatingVectorUserHistorySummarizer.makeRatingVector(p),
                                             test.getPredictions());
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
                writeOutput(buildTimer, testTimer, outputRow, evalAccums);
            } catch (IOException e) {
                logger.error("Error writing output", e);
            }
        } catch (RecommenderBuildException e) {
            logger.error("error building recommender {}: {}", algorithm, e);
            throw new RuntimeException(e);
        } finally {
            LKFileUtils.close(userTable, predictTable);
        }
    }

    private ExecutionInfo buildExecInfo() {
        ExecutionInfo.Builder bld = new ExecutionInfo.Builder();
        bld.setAlgoName(algorithm.getName())
           .setAlgoAttributes(algorithm.getAttributes())
           .setDataName(data.getName())
           .setDataAttributes(data.getAttributes());
        return bld.build();
    }

    private void writePredictions(TableWriter predictTable, long uid, SparseVector ratings, SparseVector predictions) {
        final int ncols = predictTable.getLayout().getColumnCount();
        final String[] row = new String[ncols];
        row[0] = Long.toString(uid);
        for (VectorEntry e : ratings.fast()) {
            long iid = e.getKey();
            row[1] = Long.toString(iid);
            row[2] = Double.toString(e.getValue());
            if (predictions.containsKey(iid)) {
                row[3] = Double.toString(predictions.get(iid));
            } else {
                row[3] = null;
            }
            int i = 4;
            for (Pair<Symbol,String> pair: channels) {
                Symbol c = pair.getLeft();
                if (predictions.hasChannel(c) && predictions.channel(c).containsKey(iid)) {
                    row[i] = Double.toString(predictions.channel(c).get(iid));
                } else {
                    row[i] = null;
                }
                i += 1;
            }
            try {
                predictTable.writeRow(row);
            } catch (IOException x) {
                throw new RuntimeException("error writing predictions", x);
            }
        }
    }

    private void writeOutput(StopWatch build, StopWatch test, List<Object> measures, List<TestUserMetricAccumulator> accums) throws IOException {
        TableWriter output = outputSupplier.get();

        try {
            Object[] row = new Object[output.getLayout().getColumnCount()];
            row[0] = build.getTime();
            row[1] = test.getTime();
            int col = 2;
            for (Object o: measures) {
                row[col] = o;
                col += 1;
            }
            for (TestUserMetricAccumulator acc : accums) {
                Object[] ar = acc.finalResults();
                if (ar != null) {
                    // no aggregated output is generated
                    int n = ar.length;
                    System.arraycopy(ar, 0, row, col, n);
                    col += n;
                }
            }
            output.writeRow(row);
        } finally {
            output.close();
        }
    }

    private class PredictionSupplier implements Supplier<SparseVector> {
        private final RecommenderInstance predictor;
        private final long user;
        private final LongSet items;

        public PredictionSupplier(RecommenderInstance pred, long id, LongSet is) {
            predictor = pred;
            user = id;
            items = is;
        }

        @Override
        public SparseVector get() {
            if (predictor == null) {
                throw new IllegalArgumentException("cannot compute predictions without a predictor");
            }
            SparseVector preds = predictor.getPredictions(user, items);
            if (preds == null) {
                throw new IllegalArgumentException("no predictions");
            }
            return preds;
        }
    }

    private class RecommendationSupplier implements Supplier<ScoredLongList> {
        private final RecommenderInstance recommender;
        private final long user;
        private final LongSet items;

        public RecommendationSupplier(RecommenderInstance rec, long id, LongSet is) {
            recommender = rec;
            user = id;
            items = is;
        }

        @Override
        public ScoredLongList get() {
            if (recommender == null) {
                throw new IllegalArgumentException("cannot compute recommendations without a recommender");
            }
            ScoredLongList recs = recommender.getRecommendations(user, items, numRecs);
            if (recs == null) {
                throw new IllegalArgumentException("no recommendations");
            }
            return recs;
        }
    }

    private class HistorySupplier implements Supplier<UserHistory<Event>> {
        private final DataAccessObject dao;
        private final long user;

        public HistorySupplier(DataAccessObject dao, long id) {
            this.dao = dao;
            user = id;
        }

        @Override
        public UserHistory<Event> get() {
            return dao.getUserHistory(user);
        }
    }
}
