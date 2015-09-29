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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Metric that measures the entropy of the top N recommendations across all users.
 *
 * This tell us essentially how large of a range of the items your recommender is covering.  It does not return
 * any per-user results.
 *
 * Small values indicate that the algorithm tends to prefer a small number of items which it recomments
 * to all users. Large values mean that the algorithm recommends many different items (to many different
 * users)
 *
 * The smallest value happens when the topN list is the same for all users (which would give an entropy
 * of roughly log_2(N)). The largest value happens when each item is recommended the same number of times
 * (for an entropy of roughly log_2(number of items)).
 *
 * This metric is registered with the type name `entropy`.
 */
public class TopNEntropyMetric extends TopNMetric<TopNEntropyMetric.Context> {
    /**
     * Construct a new length metric.
     */
    public TopNEntropyMetric() {
        super(null, EntropyResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, ResultList recommendations, Context context) {
        context.addUser(recommendations);
        return MetricResult.empty();
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, org.lenskit.api.Recommender recommender) {
        return new Context();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return context.finish();
    }

    public static class EntropyResult extends TypedMetricResult {
        @ResultColumn("TopN.Entropy")
        public final double entropy;
        public EntropyResult(double e) {
            entropy = e;
        }
    }

    public static class Context {
        private Long2IntMap counts = new Long2IntOpenHashMap();
        private int recCount = 0;

        private void addUser(ResultList recs) {
            for (Result s: recs) {
                counts.put(s.getId(), counts.get(s.getId()) +1);
                recCount +=1;
            }
        }

        public EntropyResult finish() {
            if (recCount > 0) {
                double entropy = 0;
                for (Long2IntMap.Entry e : counts.long2IntEntrySet()) {
                    double p = (double) e.getIntValue()/ recCount;
                    entropy -= p*Math.log(p)/Math.log(2);
                }
                return new EntropyResult(entropy);
            } else {
                return null;
            }
        }
    }
}
