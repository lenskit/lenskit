/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.eval.metrics;

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.traintest.TestUser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.List;

/**
 * Base interface for metrics, which are used by evaluations to measure their results.  Metrics may
 * have additional resources backing them, such as extra output files, and therefore must be closed.
 *
 * <p>
 * Metrics use <em>contexts</em> to track and accumulate data for experimental conditions.  For each
 * experimental condition (algorithm / data set pair), the evaluator will do the following:
 *
 * <ol>
 * <li>Create a context for the experiment using {@link #createContext(Attributed, TTDataSet, Recommender)}.</li>
 * <li>Measure each test user with {@link #measureUser(TestUser, Object)}, passing in the context
 * in which the user should be evaluated.</li>
 * <li>Output the result of each user measurement to the per-user output table (if active).</li>
 * <li>Obtain the aggregate results from this metric with {@link #getResults(Object)}.</li>
 * </ol>
 *
 * <p>
 * The context will general consist of accumulators for the aggregate results reported by a metric
 * over the entire experimental condition, such as the average of all user measurements.  It may
 * also contain additional relevant information, such as anything needed from the algorithm and
 * data set for the measurements, or additional output tables for recording extra data.
 *
 * <p>
 * Metrics themselves are generally stateless, with all state contained in the context.  In this
 * case, there is a single instance of the metric, or an instance per parameterization, and the
 * {@link #close()} method is a no-op.  Some metrics need state or output resources; such metrics
 * should define a {@link org.grouplens.lenskit.eval.traintest.MetricFactory} to instantiate them,
 * and clean up and release resources or state in {@link #close()}.
 *
 * <p>
 * Metrics may be used from multiple threads.  LensKit does not currently use multiple threads with
 * the same context, but that may change in the future.
 *
 * <p>
 * {@link AbstractMetric} provides a base implementation of this interface that allows user and
 * aggregate measurements to be defined in plan Java objects, so metrics do not need to handle
 * creating table rows themselves.
 *
 * @param <X> The type of accumulator used by this metric.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10 (rewritten in 2.1)
 */
public interface Metric<X> extends Closeable {
    /**
     * Get labels for the aggregate columns output by this evaluator.
     *
     * @return The labels for this evaluator's output, used as column headers when
     *         outputting the results table.
     */
    List<String> getColumnLabels();

    /**
     * Get labels for the per-user columns output by this evaluator.
     *
     * @return The labels for this evaluator's per-user output, used as column headers
     *         when outputting the results table.
     * @see #measureUser(TestUser, Object)
     */
    List<String> getUserColumnLabels();

    /**
     * Create the context for an experimental condition (algorithm/data set pair).
     *
     * @param algorithm The algorithm.
     * @param dataSet   The data set.
     * @param recommender The LensKit recommender, if applicable.  This can be null for an external
     *                    algorithm that does not provide a LensKit recommender.
     * @return The accumulator.  This will be passed to {@link #measureUser(TestUser, Object)}. If
     * the metric does not accumulate any results, this method can return {@code null}.
     */
    @Nullable
    X createContext(Attributed algorithm, TTDataSet dataSet, Recommender recommender);

    /**
     * Measure a user in the evaluation.
     * @param user The user to evaluate.
     * @param context The context for the active experimental condition.
     * @return The table rows resulting from this user's measurement.
     */
    @Nonnull
    List<Object> measureUser(TestUser user, X context);

    /**
     * Get the aggregate results from an accumulator.
     * @param context The context for an experimental condition.
     * @return The aggregate results from the accumulator.
     */
    @Nonnull
    List<Object> getResults(X context);
}
