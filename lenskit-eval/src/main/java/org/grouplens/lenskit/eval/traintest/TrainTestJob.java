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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Closer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.Metric;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
    private final TrainTestEvalTask task;
    private ExperimentOutputs outputs;

    /**
     * Create a new train-test eval job.
     *
     * @param task     The task this jbo belongs to.
     * @param algo     The algorithmInfo to test.
     * @param ds       The data set to use.
     */
    public TrainTestJob(TrainTestEvalTask task,
                        @Nonnull Attributed algo,
                        @Nonnull TTDataSet ds) {
        this.task = task;
        algorithmInfo = algo;
        dataSet = ds;
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
            outputs = task.getOutputs().getPrefixed(algorithmInfo, dataSet);
            TableWriter userResults = outputs.getUserWriter();
            List<Object> outputRow = Lists.newArrayList();

            logger.info("Building {} on {}", algorithmInfo, dataSet);
            StopWatch buildTimer = new StopWatch();
            buildTimer.start();
            buildRecommender();
            buildTimer.stop();
            logger.info("Built {} in {}", algorithmInfo.getName(), buildTimer);

            logger.info("Measuring {} on {}", algorithmInfo.getName(), dataSet.getName());

            StopWatch testTimer = new StopWatch();
            testTimer.start();
            List<Object> userRow = Lists.newArrayList();

            List<MetricWithAccumulator<?>> accumulators = Lists.newArrayList();

            for (Metric<?> eval: outputs.getMetrics()) {
                accumulators.add(makeMetricAccumulator(eval));
            }

            LongSet testUsers = dataSet.getTestData().getUserDAO().getUserIds();
            final NumberFormat pctFormat = NumberFormat.getPercentInstance();
            pctFormat.setMaximumFractionDigits(2);
            pctFormat.setMinimumFractionDigits(2);
            final int nusers = testUsers.size();
            logger.info("Testing {} on {} ({} users)", algorithmInfo, dataSet, nusers);
            int ndone = 0;
            for (LongIterator iter = testUsers.iterator(); iter.hasNext();) {
                if (Thread.interrupted()) {
                    throw new InterruptedException("eval job interrupted");
                }
                long uid = iter.nextLong();
                userRow.add(uid);
                userRow.add(null); // placeholder for the per-user time
                assert userRow.size() == 2;

                Stopwatch userTimer = Stopwatch.createStarted();
                TestUser test = getUserResults(uid);

                userRow.add(test.getTrainHistory().size());
                userRow.add(test.getTestHistory().size());

                for (MetricWithAccumulator<?> accum : accumulators) {
                    List<Object> ures = accum.measureUser(test);
                    if (ures != null) {
                        userRow.addAll(ures);
                    }
                }
                userTimer.stop();
                userRow.set(1, userTimer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
                if (userResults != null) {
                    try {
                        userResults.writeRow(userRow);
                    } catch (IOException e) {
                        throw new RuntimeException("error writing user row", e);
                    }
                }
                userRow.clear();

                ndone += 1;
                if (ndone % 100 == 0) {
                    testTimer.split();
                    double time = testTimer.getSplitTime();
                    double tpu = time / ndone;
                    double tleft = (nusers - ndone) * tpu;
                    logger.info("tested {} of {} users ({}), ETA {}",
                                ndone, nusers, pctFormat.format(((double) ndone) / nusers),
                                DurationFormatUtils.formatDurationHMS((long) tleft));
                }
            }
            testTimer.stop();
            logger.info("Tested {} in {}", algorithmInfo.getName(), testTimer);

            writeMetricValues(buildTimer, testTimer, outputRow, accumulators);
            bus.post(JobEvents.finished(this));
        } catch (Throwable th) {
            bus.post(JobEvents.failed(this, th));
            throw closer.rethrow(th, RecommenderBuildException.class);
        } finally {
            try {
                cleanup();
            } finally {
                outputs = null;
                closer.close();
            }
        }
    }

    /**
     * Create an accumulator for a metric.
     * @param metric The metric.
     * @return The metric accumulator.
     */
    protected <A> MetricWithAccumulator<A> makeMetricAccumulator(Metric<A> metric) {
        return new MetricWithAccumulator<A>(metric, metric.createContext(algorithmInfo, dataSet, null));
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

    private void writeMetricValues(StopWatch build, StopWatch test, List<Object> measures, List<MetricWithAccumulator<?>> accums) throws IOException {
        TableWriter results = outputs.getResultsWriter();

        List<Object> row = Lists.newArrayList();
        row.add(build.getTime());
        row.add(test.getTime());
        row.addAll(measures);
        for (MetricWithAccumulator<?> acc : accums) {
            row.addAll(acc.getResults());
        }
        results.writeRow(row);
    }

    @Override
    public String toString() {
        return String.format("test %s on %s", algorithmInfo, dataSet);
    }

    protected static class MetricWithAccumulator<A> {
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

        public List<Object> getResults() {
            return metric.getResults(accumulator);
        }
    }
}
