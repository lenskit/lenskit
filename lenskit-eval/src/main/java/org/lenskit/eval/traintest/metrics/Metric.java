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
package org.lenskit.eval.traintest.metrics;

import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Base interface for metrics, which are used by evaluations to measure results.
 *
 * <p>
 * Metrics use <em>contexts</em> to track and accumulate data for experimental conditions.  For each
 * experimental condition (algorithm / data set pair), the evaluator will do the following:
 *
 * <ol>
 * <li>Create a context for the experiment using {@link #createContext(Attributed, TTDataSet, Recommender)}.</li>
 * <li>Measure each result with {@link #measureResult(long, Result, Object)}.</li>
 * <li>Output the result of each user measurement to the per-result output table (if active).</li>
 * <li>Obtain the aggregate results from this metric with {@link #getAggregateMeasurements(Object)}.</li>
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
 * case, there is a single instance of the metric, or an instance per parameterization.
 *
 * <p>
 * Metrics may be used from multiple threads.  LensKit may use multiple threads with
 * the same context.
 *
 * <p>
 * {@link AbstractMetric} provides a base implementation of this interface that allows user and
 * aggregate measurements to be defined in plan Java objects, so metrics do not need to handle
 * creating table rows themselves.
 *
 * @param <X> The type of context used by this metric.
 */
public interface Metric<X> {
    /**
     * Get labels for the aggregate columns output by this evaluator.
     *
     * @return The labels for this evaluator's output, used as column headers when
     *         outputting the results table.
     */
    List<String> getAggregateColumnLabels();

    /**
     * Get labels for the per-result columns output by this metric.
     *
     * @return The labels for this metric's per-result output, used as column headers
     *         when outputting the results table.
     * @see #measureResult(long, Result, Object)
     */
    List<String> getResultColumnLabels();

    /**
     * Create the context for an experimental condition (algorithm/data set pair).
     *
     * @param algorithm The algorithm.
     * @param dataSet   The data set.
     * @param recommender The LensKit recommender, if applicable.  This can be null for an external
     *                    algorithm that does not provide a LensKit recommender.
     * @return The accumulator.  This will be passed to the individual measurement methods. If
     * the metric does need to accumulate any results, this method can return {@code null}.
     */
    @Nullable
    X createContext(Attributed algorithm, TTDataSet dataSet, Recommender recommender);

    /**
     * Measure a single result.  The result may come from either prediction or recommendation.
     * @param userId The user ID.
     * @param result The result to measure.
     * @return A list of fields to add to the result's output.
     */
    List<Object> measureResult(long userId, Result result, X context);

    /**
     * Get the aggregate results from an accumulator.
     * @param context The context for an experimental condition.
     * @return The aggregate results from the accumulator.
     */
    @Nonnull
    List<Object> getAggregateMeasurements(X context);
}
