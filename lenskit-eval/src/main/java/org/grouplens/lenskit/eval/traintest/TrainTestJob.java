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
import com.google.common.eventbus.EventBus;
import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.eval.metrics.MetricAccumulator;
import org.grouplens.lenskit.eval.metrics.topn.ItemSelectors;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Run a single train-test evaluation of a single algorithmInfo.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
abstract class TrainTestJob implements Callable<Void> {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestJob.class);

    protected final Attributed algorithmInfo;
    protected final TTDataSet dataSet;

    protected final MeasurementSuite measurements;
    private final ExperimentOutputs output;
    private final TrainTestEvalTask task;

    /**
     * Create a new train-test eval job.
     *
     * @param algo     The algorithmInfo to test.
     * @param ds       The data set to use.
     * @param out      The table writer to receive output. This writer is expected to be prefixed
     *                 with algorithmInfo and group ID data, so only the times and eval outputProvider
     *                 needs to be written.
     * @param measures The measurements to take.
     * @param out      The evaluator outputs.
     */
    public TrainTestJob(TrainTestEvalTask task,
                        @Nonnull Attributed algo,
                        @Nonnull TTDataSet ds,
                        @Nonnull MeasurementSuite measures,
                        @Nonnull ExperimentOutputs out) {
        this.task = task;
        algorithmInfo = algo;
        dataSet = ds;
        measurements = measures;
        output = out;
    }

    /**
     * Get the eval task associated with this event.
     *
     * @return The task.
     */
    public TrainTestEvalTask getTask() {
        return task;
    }

    @Override
    public Void call() throws IOException, RecommenderBuildException {
        runEvaluation();
        return null;
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void runEvaluation() throws IOException, RecommenderBuildException {
        EventBus bus = task.getProject().getEventBus();
        bus.post(JobEvents.started(this));
        Closer closer = Closer.create();
        try {
            TableWriter userResults = output.getUserWriter();
            List<Object> outputRow = Lists.newArrayList();

            logger.info("Building {} on {}", algorithmInfo, dataSet);
            StopWatch buildTimer = new StopWatch();
            buildTimer.start();
            buildRecommender();
            buildTimer.stop();
            logger.info("Built {} in {}", algorithmInfo.getName(), buildTimer);

            logger.info("Measuring {} on {}", algorithmInfo.getName(), dataSet.getName());

            logger.info("Testing {}", algorithmInfo.getName());
            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<Object> userRow = Lists.newArrayList();

            List<MetricWithAccumulator<?>> accumulators = Lists.newArrayList();

            for (Metric<?> eval: output.getMetrics()) {
                accumulators.add(makeMetricAccumulator(eval));
            }

            LongSet testUsers = dataSet.getTestData().getUserDAO().getUserIds();
            for (LongIterator iter = testUsers.iterator(); iter.hasNext();) {
                if (Thread.interrupted()) {
                    throw new InterruptedException("eval job interrupted");
                }
                long uid = iter.nextLong();
                userRow.add(uid);

                TestUser test = getUserResults(uid);

                for (MetricWithAccumulator<?> accum : accumulators) {
                    List<Object> ures = accum.measureUser(test);
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
            logger.info("Tested {} in {}", algorithmInfo.getName(), testTimer);

            writeMetricValues(buildTimer, testTimer, outputRow, accumulators);
            bus.post(JobEvents.finished(this));
        } catch (Throwable th) {
            bus.post(JobEvents.failed(this, th));
            throw closer.rethrow(th, RecommenderBuildException.class);
        } finally {
            cleanup();
            closer.close();
        }
    }

    /**
     * Create an accumulator for a metric.
     * @param metric The metric.
     * @return The metric accumulator.
     */
    protected <A extends MetricAccumulator> MetricWithAccumulator<A> makeMetricAccumulator(Metric<A> metric) {
        return new MetricWithAccumulator<A>(metric, metric.createAccumulator(algorithmInfo, dataSet, null));
    }

    /**
     * Build the recommender.
     * @throws RecommenderBuildException if there is an error building the recommender.
     * @throws IllegalStateException if the recommender has already been built.
     */
    protected abstract void buildRecommender() throws RecommenderBuildException;

    /**
     * Get the results for a particular user.
     * @param uid The user id.
     * @return The user's results.
     * @throws IllegalArgumentException if the user is not a valid test user.
     * @throws IllegalStateException if the recommender has not yet been built.
     */
    protected abstract TestUser getUserResults(long uid);

    /**
     * Clean up the job after it is finished (freeing memory, etc.).
     */
    protected abstract void cleanup();

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
        if (recs == null) return;

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

    private void writeMetricValues(StopWatch build, StopWatch test, List<Object> measures, List<MetricWithAccumulator<?>> accums) throws IOException {
        TableWriter results = output.getResultsWriter();

        List<Object> row = Lists.newArrayList();
        row.add(build.getTime());
        row.add(test.getTime());
        row.addAll(measures);
        for (MetricWithAccumulator<?> acc : accums) {
            row.addAll(acc.getAccumulator().finish());
        }
        results.writeRow(row);
    }

    @Override
    public String toString() {
        return String.format("test %s on %s", algorithmInfo, dataSet);
    }

    protected static class MetricWithAccumulator<A extends MetricAccumulator> {
        private final Metric<A> metric;
        private final A accumulator;

        public MetricWithAccumulator(Metric<A> m, A a) {
            metric = m;
            accumulator = a;
        }

        public List<Object> measureUser(TestUser user) {
            return metric.measureUser(user, accumulator);
        }

        public Metric<A> getMetric() {
            return metric;
        }

        public A getAccumulator() {
            return accumulator;
        }
    }
}
