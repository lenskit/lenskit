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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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
public class TopNEntropyMetric extends ListOnlyTopNMetric<TopNEntropyMetric.Context> {
    /**
     * Construct a new length metric.
     */
    public TopNEntropyMetric() {
        super(null, EntropyResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recommendations, Context context) {
        context.addUser(recommendations);
        return MetricResult.empty();
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Context();
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return MetricResult.fromNullable(context.finish());
    }

    public static class EntropyResult extends TypedMetricResult {
        @MetricColumn("TopN.Entropy")
        public final double entropy;
        public EntropyResult(double e) {
            entropy = e;
        }
    }

    public static class Context {
        private Long2IntMap counts = new Long2IntOpenHashMap();
        private int recCount = 0;

        private synchronized void addUser(List<Long> recs) {
            for (long item: recs) {
                counts.put(item, counts.get(item) +1);
                recCount +=1;
            }
        }

        @Nullable
        public synchronized EntropyResult finish() {
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
