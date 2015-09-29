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
package org.lenskit.eval.traintest.recommend;

import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.lenskit.api.Recommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Metric that measures how popular the items in the TopN list are.
 *
 * This metric is registered with the type name `popularity`.
 */
public class TopNPopularityMetric extends TopNMetric<TopNPopularityMetric.Context> {
    public TopNPopularityMetric() {
        super(PopResult.class, PopResult.class);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, Recommender recommender) {
        LenskitRecommender lkrec = (LenskitRecommender) recommender;
        RatingSummary sum = lkrec.get(RatingSummary.class);
        return new Context(sum);
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultList recs, Context context) {
        if (recs == null || recs.isEmpty()) {
            return MetricResult.empty();
        }
        double pop = 0;
        for (Result r: recs) {
            pop += context.summary.getItemRatingCount(r.getId());
        }
        pop = pop / recs.size();

        context.mean.add(pop);
        return new PopResult(pop);
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new PopResult(context.mean.getMean());
    }

    public static class PopResult extends TypedMetricResult {
        @ResultColumn("TopN.MeanPopularity")
        public final double mean;

        public PopResult(double mu) {
            mean = mu;
        }
    }
    
    public class Context {
        final RatingSummary summary;
        final MeanAccumulator mean = new MeanAccumulator();

        public Context(RatingSummary sum) {
            this.summary = sum;
        }
    }
}
