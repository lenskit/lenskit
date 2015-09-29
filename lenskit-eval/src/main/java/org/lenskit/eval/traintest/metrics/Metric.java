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

import com.google.common.collect.ImmutableList;
import org.lenskit.api.Recommender;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Interface for common aspects of metrics, which are used to measure individual or per-user results.  Each metric
 * can measure results or users, and contribute aggregate values to the global aggregate measurement table.
 *
 * This class should not be extended directly; rather, one of its subclasses should be extended.  This hierarchy uses
 * abstract classes instead of interfaces for two reasons: first, to provide useful default behavior, and second,
 * because implementing multiple types of metrics in a single class is likely to be erroneous.
 *
 * Metrics use <em>contexts</em> to track and accumulate data for experimental conditions.  For each
 * experimental condition (algorithm / data set pair), the evaluator will do the following:
 *
 * <ol>
 * <li>Create a context for the experiment using {@link #createContext(AlgorithmInstance, DataSet, Recommender)}.</li>
 * <li>Measure each user or result with the appropriate method.
 * <li>Output the result of each user or result measurement to appropriate output file (if active).</li>
 * <li>Obtain the aggregate results from this metric with {@link #getAggregateMeasurements(Object)} and add them
 * to the global output file.</li>
 * </ol>
 *
 * The context will generally consist of accumulators for the aggregate results reported by a metric
 * over the entire experimental condition, such as the average of all user measurements.  It may
 * also contain additional relevant information, such as anything needed from the algorithm and
 * data set for the measurements, or additional output tables for recording extra data.
 *
 *  * Metrics themselves are generally stateless, with all state contained in the context.  In this
 * case, there is a single instance of the metric, or an instance per parameterization.
 *
 * Metrics may be used from multiple threads.  LensKit may use multiple threads with
 * the same context.
 *
 * @param <X> The type of context used by this metric.
 */
public abstract class Metric<X> {
    private final List<String> columnLabels;
    private final List<String> aggregateColumnLabels;

    protected Metric(List<String> labels, List<String> aggLabels) {
        columnLabels = ImmutableList.copyOf(labels);
        aggregateColumnLabels = ImmutableList.copyOf(aggLabels);
    }

    /**
     * Get labels for the aggregate columns output by this metric.
     *
     * @return The labels for this metric's aggregate output, used as column headers when
     *         outputting the results table.
     */
    public List<String> getAggregateColumnLabels() {
        return aggregateColumnLabels;
    }

    /**
     * Get the labels for the per-user or per-result columns output by this metric.
     *
     * @return The labels for this metric's output, used as column headers in the appropriate table.
     */
    public List<String> getColumnLabels() {
        return columnLabels;
    }

    /**
     * Get the classes on which this metric depends.  These will be added to the roots of each algorithm configuration.
     * @return The required roots for this metric.
     */
    public Set<Class<?>> getRequiredRoots() {
        return Collections.emptySet();
    }

    /**
     * Create the context for an experimental condition (algorithm/data set pair).  The default implementation
     * returns `null`.
     *
     * @param algorithm The algorithm.
     * @param dataSet   The data set.
     * @param recommender The LensKit recommender, if applicable.  This can be null for an external
     *                    algorithm that does not provide a LensKit recommender.
     * @return The accumulator.  This will be passed to the individual measurement methods. If
     * the metric does need to accumulate any results, this method can return {@code null}.
     */
    @Nullable
    public X createContext(AlgorithmInstance algorithm, DataSet dataSet, Recommender recommender) {
        return null;
    }

    /**
     * Get the aggregate results from an accumulator.  The default implementation returns {@link MetricResult#empty()}.
     *
     * @param context The context for an experimental condition.
     * @return The aggregate results from the accumulator.
     */
    @Nonnull
    public MetricResult getAggregateMeasurements(X context) {
        return MetricResult.empty();
    }
}
