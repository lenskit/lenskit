/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.eval.traintest.metrics;

import com.google.common.collect.ImmutableList;
import org.lenskit.api.RecommenderEngine;
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
 * <li>Create a context for the experiment using {@link #createContext(AlgorithmInstance, DataSet, RecommenderEngine)}.</li>
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
 * Metrics themselves are generally stateless, with all state contained in the context.  In this
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
     * **Note:** Contexts must be thread-safe, in that multiple concurrent calls to the appropriate user-measurement
     * function with the same context must be safe.  This can be handled either by the context itself, or by the
     * user-measurement function.
     *
     * @param algorithm The algorithm.
     * @param dataSet   The data set.
     * @param engine    The LensKit recommender engine, if applicable.  This can be null for an external
     *                  algorithm that does not provide a LensKit recommender.
     * @return The context. This will be passed to the individual measurement methods. If
     * the metric does need to accumulate any results, this method can return {@code null}.
     */
    @Nullable
    public X createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
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