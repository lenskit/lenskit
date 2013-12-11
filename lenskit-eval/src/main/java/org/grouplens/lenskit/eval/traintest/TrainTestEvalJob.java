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
import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.eval.ExecutionInfo;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.algorithm.RecommenderInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelectors;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Run a single train-test evaluation of a single algorithm.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
class TrainTestEvalJob implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalJob.class);

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
    @Nonnull
    private final Supplier<TableWriter> recommendOutputSupplier;
    private final Provider<PreferenceSnapshot> snapshot;

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
     */
    public TrainTestEvalJob(@Nonnull AlgorithmInstance algo,
                            @Nonnull List<TestUserMetric> evals,
                            @Nonnull List<ModelMetric> mMetrics,
                            @Nonnull List<Pair<Symbol,String>> chans,
                            @Nonnull TTDataSet ds, Provider<PreferenceSnapshot> snap,
                            @Nonnull Supplier<TableWriter> out,
                            @Nonnull Supplier<TableWriter> userOut,
                            @Nonnull Supplier<TableWriter> predOut,
                            @Nonnull Supplier<TableWriter> recoOut) {
        algorithm = algo;
        evaluators = evals;
        modelMetrics = mMetrics;
        channels = chans;
        data = ds;
        snapshot = snap;
        outputSupplier = out;
        userOutputSupplier = userOut;
        predictOutputSupplier = predOut;
        recommendOutputSupplier = recoOut;
    }

    @Override
    public Void call() throws IOException, RecommenderBuildException {
        runEvaluation();
        return null;
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void runEvaluation() throws IOException, RecommenderBuildException {
        Closer closer = Closer.create();
        try {
            TableWriter userTable = userOutputSupplier.get();
            if (userTable != null) {
                closer.register(userTable);
            }
            TableWriter predictTable = predictOutputSupplier.get();
            if (predictTable != null) {
                closer.register(predictTable);
            }
            TableWriter recommendTable = recommendOutputSupplier.get();
            if (recommendTable != null) {
                closer.register(recommendTable);
            }


            List<Object> outputRow = Lists.newArrayList();

            ExecutionInfo execInfo = buildExecInfo();

            logger.info("Building {} on {}", algorithm.getName(), data.getName());
            StopWatch buildTimer = new StopWatch();
            buildTimer.start();
            RecommenderInstance rec = algorithm.makeTestableRecommender(data, snapshot, execInfo);
            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Measuring {} on {}", algorithm.getName(), data.getName());
            for (ModelMetric metric: modelMetrics) {
                outputRow.addAll(metric.measureAlgorithm(algorithm, data, rec.getRecommender()));
            }

            logger.info("Testing {}", algorithm.getName());
            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<TestUserMetricAccumulator> evalAccums = new ArrayList<TestUserMetricAccumulator>(evaluators.size());

            List<Object> userRow = new ArrayList<Object>();

            UserEventDAO testUsers = data.getTestData().getUserEventDAO();
            for (TestUserMetric eval : evaluators) {
                TestUserMetricAccumulator accum = eval.makeAccumulator(algorithm, data);
                evalAccums.add(accum);
            }

            Cursor<UserHistory<Event>> userProfiles = closer.register(testUsers.streamEventsByUser());
            for (UserHistory<Event> p : userProfiles) {
                assert userRow.isEmpty();
                userRow.add(p.getUserId());

                long uid = p.getUserId();

                TestUser test = rec.getUserResults(uid);

                for (TestUserMetricAccumulator accum : evalAccums) {
                    List<Object> ures = accum.evaluate(test);
                    if (ures != null) {
                        userRow.addAll(ures);
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
                    SparseVector preds = test.getPredictions();
                    if (preds != null) {
                        writePredictions(predictTable, uid,
                                         RatingVectorUserHistorySummarizer.makeRatingVector(p),
                                         test.getPredictions());
                    }
                }

                if(recommendTable != null) {
                    // FIXME: for now, the recommend ouput default to predict on all items excluding rated items
                    List<ScoredId> reco = test.getRecommendations(-1, ItemSelectors.allItems(),
                                                                  ItemSelectors.trainingItems());
                    if (reco != null) {
                        writeRecommendations(recommendTable, uid, reco);
                    }
                }
            }
            testTimer.stop();
            logger.info("Tested {} in {}", algorithm.getName(), testTimer);

            writeMetricValues(buildTimer, testTimer, outputRow, evalAccums);
        } catch (Throwable th) {
            throw closer.rethrow(th, RecommenderBuildException.class);
        } finally {
            closer.close();
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

    private void writePredictions(TableWriter predictTable, long uid, SparseVector ratings, SparseVector predictions) throws IOException {
        final int ncols = predictTable.getLayout().getColumnCount();
        final Object[] row = new String[ncols];
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
                if (predictions.hasChannelVector(c) && predictions.getChannelVector(c).containsKey(iid)) {
                    row[i] = Double.toString(predictions.getChannelVector(c).get(iid));
                } else {
                    row[i] = null;
                }
                i += 1;
            }
            predictTable.writeRow(row);
        }
    }

    private void writeRecommendations(TableWriter recommendTable, long uid, List<ScoredId> recs) throws IOException {
        final int ncols = recommendTable.getLayout().getColumnCount();
        final String[] row = new String[ncols];
        row[0] = Long.toString(uid);
        int counter = 1;
        for (ScoredId p : CollectionUtils.fast(recs)) {
            long iid = p.getId();
            row[1] = Long.toString(iid);
            row[2] = String.valueOf(counter);
            counter ++;
            row[3] = Double.toString(p.getScore());
            recommendTable.writeRow(row);
        }
    }

    private void writeMetricValues(StopWatch build, StopWatch test, List<Object> measures, List<TestUserMetricAccumulator> accums) throws IOException {
        TableWriter output = outputSupplier.get();

        try {
            List<Object> row = Lists.newArrayListWithCapacity(output.getLayout().getColumnCount());
            row.add(build.getTime());
            row.add(test.getTime());
            row.addAll(measures);
            for (TestUserMetricAccumulator acc : accums) {
                row.addAll(acc.finalResults());
            }
            output.writeRow(row);
        } finally {
            output.close();
        }
    }

    @Override
    public String toString() {
        return String.format("test %s on %s", algorithm, data);
    }
}
