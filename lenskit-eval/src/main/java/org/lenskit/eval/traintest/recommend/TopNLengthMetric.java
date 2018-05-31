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
package org.lenskit.eval.traintest.recommend;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Metric that measures how long a TopN list actually is.
 *
 * This metric is registered with the type name `length`.
 */
public class TopNLengthMetric extends ListOnlyTopNMetric<Mean> {
    /**
     * Construct a new length metric.
     */
    public TopNLengthMetric() {
        super(LengthResult.class, LengthResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recommendations, Mean context) {
        int n = recommendations.size();
        synchronized (context) {
            context.increment(n);
        }
        return new LengthResult(n);
    }

    @Nullable
    @Override
    public Mean createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Mean();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Mean context) {
        return new LengthResult(context.getResult());
    }

    public static class LengthResult extends TypedMetricResult {
        @MetricColumn("TopN.ActualLength")
        public final double length;

        public LengthResult(double len) {
            length = len;
        }
    }
}
