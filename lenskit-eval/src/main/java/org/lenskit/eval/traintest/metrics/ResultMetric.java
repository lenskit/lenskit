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

import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Base class for metrics that measure individual recommendation or prediction results.
 *
 * @param <X> The context type.
 */
public abstract class ResultMetric<X> extends Metric<X> {
    /**
     * Construct a new result metric.
     * @param labels Column labels.
     * @param aggLabels Aggregate column labels.
     */
    protected ResultMetric(List<String> labels, List<String> aggLabels) {
        super(labels, aggLabels);
    }

    /**
     * Construct a new result metric.
     * @param resType The result type for measuring results, or `null` for no measurement.
     * @param aggType The result type for aggregate measurements, or `null` for no measurement.
     */
    protected ResultMetric(Class<? extends TypedMetricResult> resType,
                           Class<? extends TypedMetricResult> aggType) {
        super(TypedMetricResult.getColumns(resType),
              TypedMetricResult.getColumns(aggType));
    }

    /**
     * Measure a single result.  The result may come from either prediction or recommendation.
     * @param userId The user ID.
     * @param result The result to measure.
     * @return A list of fields to add to the result's output.
     */
    @Nonnull
    public abstract MetricResult measureResult(long userId, Result result, X context);
}
