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

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
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
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Run a single train-test evaluation of a single algorithm.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
class TrainTestJob implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestJob.class);

    private final AlgorithmInstance algorithm;
    private final TTDataSet data;
    private final Provider<PreferenceSnapshot> snapshot;
    private final MeasurementSuite measurements;
    private final ExperimentOutputs output;

    /**
     * Create a new train-test eval job.
     *
     * @param algo     The algorithm to test.
     * @param ds       The data set to use.
     * @param snap     Supplier providing access to a shared rating snapshot to use in the build
     *                 process.
     * @param out      The table writer to receive output. This writer is expected to be prefixed
     *                 with algorithm and group ID data, so only the times and eval outputProvider
     *                 needs to be written.
     * @param measures The measurements to take.
     * @param out      The evaluator outputs.
     */
    public TrainTestJob(@Nonnull AlgorithmInstance algo,
                        @Nonnull TTDataSet ds,
                        Provider<PreferenceSnapshot> snap,
                        @Nonnull MeasurementSuite measures,
                        @Nonnull ExperimentOutputs out) {
        algorithm = algo;
        data = ds;
        snapshot = snap;
        measurements = measures;
        output = out;
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
            TableWriter userResults = output.getUserWriter();
            List<Object> outputRow = Lists.newArrayList();
            ExecutionInfo execInfo = buildExecInfo();

            logger.info("Building {} on {}", algorithm.getName(), data.getName());
            StopWatch buildTimer = new StopWatch();
            buildTimer.start();
            RecommenderInstance rec = algorithm.makeTestableRecommender(data, snapshot, execInfo);
            buildTimer.stop();
            logger.info("Built {} in {}", algorithm.getName(), buildTimer);

            logger.info("Measuring {} on {}", algorithm.getName(), data.getName());
            for (ModelMetric metric: measurements.getModelMetrics()) {
                outputRow.addAll(metric.measureAlgorithm(algorithm, data, rec.getRecommender()));
            }

            logger.info("Testing {}", algorithm.getName());
            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<TestUserMetricAccumulator> evalAccums = Lists.newArrayList();

            List<Object> userRow = Lists.newArrayList();

            UserEventDAO testUsers = data.getTestData().getUserEventDAO();
            for (TestUserMetric eval: measurements.getTestUserMetrics()) {
                TestUserMetricAccumulator accum = eval.makeAccumulator(algorithm, data);
                evalAccums.add(accum);
            }

            Cursor<UserHistory<Event>> userProfiles = closer.register(testUsers.streamEventsByUser());
            for (UserHistory<Event> user: userProfiles) {
                assert userRow.isEmpty();
                userRow.add(user.getUserId());

                long uid = user.getUserId();

                TestUser test = rec.getUserResults(uid);

                for (TestUserMetricAccumulator accum : evalAccums) {
                    List<Object> ures = accum.evaluate(test);
                    if (ures != null) {
                        userRow.addAll(ures);
                    }
                }
                if (userResults != null) {
                    try {
                        userResults.writeRow(userRow);
                    } catch (IOException e) {
                        throw new RuntimeException("error writing user row", e);
                    }
                }
                userRow.clear();

                writePredictions(test);
                writeRecommendations(test);
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

    private void writePredictions(TestUser user) throws IOException {
        TableWriter predictTable = output.getPredictionWriter();
        if (predictTable == null) return;

        SparseVector predictions = user.getPredictions();
        if (predictions == null) return;

        SparseVector ratings = user.getTestRatings();

        final int ncols = predictTable.getLayout().getColumnCount();
        final Object[] row = new String[ncols];
        row[0] = Long.toString(user.getUserId());
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
            for (Pair<Symbol,String> pair: measurements.getPredictionChannels()) {
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

    private void writeRecommendations(TestUser user) throws IOException {
        TableWriter recommendTable = output.getRecommendationWriter();
        if (recommendTable == null) return;

        // FIXME: for now, the recommend ouput default to predict on all items excluding rated items
        List<ScoredId> recs = user.getRecommendations(-1, ItemSelectors.allItems(),
                                                      ItemSelectors.trainingItems());
        final int ncols = recommendTable.getLayout().getColumnCount();
        final String[] row = new String[ncols];
        row[0] = Long.toString(user.getUserId());
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
        TableWriter results = output.getResultsWriter();

        List<Object> row = Lists.newArrayList();
        row.add(build.getTime());
        row.add(test.getTime());
        row.addAll(measures);
        for (TestUserMetricAccumulator acc : accums) {
            row.addAll(acc.finalResults());
        }
        results.writeRow(row);
    }

    @Override
    public String toString() {
        return String.format("test %s on %s", algorithm, data);
    }
}
