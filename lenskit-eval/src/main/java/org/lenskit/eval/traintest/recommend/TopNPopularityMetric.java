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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.lenskit.LenskitRecommender;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Metric that measures how popular the items in the TopN list are.
 *
 * This metric is registered with the type name `popularity`.
 */
public class TopNPopularityMetric extends ListOnlyTopNMetric<TopNPopularityMetric.Context> {
    public TopNPopularityMetric() {
        super(PopResult.class, PopResult.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Class<?>> getRequiredRoots() {
        return (Set) Collections.singleton(RatingSummary.class);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Context();
    }

    @Nonnull
    @Override
    public MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recs, Context context) {
        RatingSummary summary = null;
        if (rec instanceof LenskitRecommender) {
            summary = ((LenskitRecommender) rec).get(RatingSummary.class);
        }
        if (recs == null || recs.isEmpty() || summary == null) {
            return MetricResult.empty();
        }
        double pop = 0;
        LongIterator iter = recs.iterator();
        while (iter.hasNext()) {
            pop += summary.getItemRatingCount(iter.nextLong());
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
        @MetricColumn("TopN.MeanPopularity")
        public final double mean;

        public PopResult(double mu) {
            mean = mu;
        }
    }
    
    public class Context {
        final MeanAccumulator mean = new MeanAccumulator();

        public Context() {
        }
    }
}
