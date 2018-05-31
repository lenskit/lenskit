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

import com.fasterxml.jackson.annotation.JsonCreator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderEngine;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Compute the mean reciprocal rank.
 * 
 * This metric is registered with the type name `mrr`.  It has two configuration parameters:
 *
 * `suffix`
 * :   a suffix to append to the column name
 *
 * `goodItems`
 * :   an item selector expression. The default is the user's test items.
 */
public class TopNMRRMetric extends ListOnlyTopNMetric<TopNMRRMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TopNMRRMetric.class);

    private final ItemSelector goodItems;
    private final String suffix;

    /**
     * Construct a new MRR metric using the user's test items as good.
     */
    public TopNMRRMetric() {
        this(ItemSelector.userTestItems(), null);
    }

    /**
     * Construct an MRR metric from a spec.
     * @param spec The metric specl
     */
    @JsonCreator
    public TopNMRRMetric(PRMetricSpec spec) {
        this(ItemSelector.compileSelector(StringUtils.defaultString(spec.getGoodItems(), "user.testItems")),
             spec.getSuffix());
    }

    /**
     * Construct a new recall and precision top n metric
     * @param goodItems The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     * @param sfx A suffix to append to the metric.
     */
    public TopNMRRMetric(ItemSelector goodItems, String sfx) {
        super(UserResult.class, AggregateResult.class, sfx);
        this.goodItems = goodItems;
        suffix = sfx;
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Context(dataSet.getAllItems());
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new AggregateResult(context).withSuffix(suffix);
    }

    @Nonnull
    @Override
    public MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recommendations, Context context) {
        LongSet good = goodItems.selectItems(context.universe, rec, user);
        if (good.isEmpty()) {
            logger.warn("no good items for user {}", user.getUserId());
        }

        Integer rank = null;
        int i = 0;
        for (long item: recommendations) {
            i++;
            if (good.contains(item)) {
                rank = i;
                break;
            }
        }

        UserResult result = new UserResult(rank);
        context.addUser(result);
        return result.withSuffix(suffix);
    }

    public static class UserResult extends TypedMetricResult {
        @MetricColumn("Rank")
        public final Integer rank;

        public UserResult(Integer r) {
            rank = r;
        }

        @MetricColumn("RecipRank")
        public double getRecipRank() {
            return rank == null ? 0 : 1.0 / rank;
        }
    }

    public static class AggregateResult extends TypedMetricResult {
        /**
         * The MRR over all users.  Users for whom no good items are included, and have a reciprocal
         * rank of 0.
         */
        @MetricColumn("MRR")
        public final double mrr;

        public AggregateResult(Context accum) {
            this.mrr = accum.allMean.getResult();
        }
    }

    public static class Context {
        private final LongSet universe;
        private final Mean allMean = new Mean();

        Context(LongSet universe) {
            this.universe = universe;
        }

        synchronized void addUser(UserResult ur) {
            allMean.increment(ur.getRecipRank());
        }
    }
}
