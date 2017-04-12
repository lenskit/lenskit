/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.traintest.recommend;

import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
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

/**
 * Metric that measures how long a TopN list actually is.
 *
 * This metric is registered with the type name `length`.
 */
public class TopNLengthMetric extends ListOnlyTopNMetric<MeanAccumulator> {
    /**
     * Construct a new length metric.
     */
    public TopNLengthMetric() {
        super(LengthResult.class, LengthResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recommendations, MeanAccumulator context) {
        int n = recommendations.size();
        context.add(n);
        return new LengthResult(n);
    }

    @Nullable
    @Override
    public MeanAccumulator createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new MeanAccumulator();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(MeanAccumulator context) {
        return new LengthResult(context.getMean());
    }

    public static class LengthResult extends TypedMetricResult {
        @MetricColumn("TopN.ActualLength")
        public final double length;

        public LengthResult(double len) {
            length = len;
        }
    }
}
