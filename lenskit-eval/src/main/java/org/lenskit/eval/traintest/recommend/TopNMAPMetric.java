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
 * Compute the mean average precision.
 *
 * The algorithm computed here is equivalent to <a href="https://github.com/benhamner/Metrics/blob/master/Python/ml_metrics/average_precision.py">Ben Hammer's Python implementation</a>,
 * as referenced by Kaggle.
 *
 * This metric is registered under the name `map`.  It has two configuration parameters:
 *
 * `suffix`
 * :   a suffix to append to the column name
 *
 * `goodItems`
 * :   an item selector expression. The default is the user's test items.
 */
public class TopNMAPMetric extends ListOnlyTopNMetric<TopNMAPMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TopNMAPMetric.class);

    private final String suffix;
    private final ItemSelector goodItems;

    /**
     * Construct a new MAP metric with the user's test items as good.
     */
    public TopNMAPMetric() {
        this(ItemSelector.userTestItems(), null);
    }

    /**
     * Create a metric from a spec.
     * @param spec The specification.
     */
    @JsonCreator
    public TopNMAPMetric(PRMetricSpec spec) {
        this(ItemSelector.compileSelector(StringUtils.defaultString(spec.getGoodItems(), "user.testItems")),
             spec.getSuffix());
    }

    /**
     * Construct a new mean average precision top n metric
     * @param sfx the suffix label for this evaluation, or {@code null} for no suffix.
     * @param good The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     */
    public TopNMAPMetric(ItemSelector good, String sfx) {
        super(UserResult.class, AggregateResult.class, sfx);
        suffix = sfx;
        goodItems = good;
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, RecommenderEngine engine) {
        return new Context(dataSet.getAllItems(), engine);
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new AggregateResult(context).withSuffix(suffix);
    }

    @Nonnull
    @Override
    public MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recs, Context context) {
        LongSet good = goodItems.selectItems(context.universe, rec, user);
        if (good.isEmpty()) {
            logger.warn("no good items for user {}", user.getUserId());
            return new UserResult(0);
        }

        if (recs == null || recs.isEmpty()) {
            return MetricResult.empty();
        }

        int n = 0;
        double ngood = 0;
        double sum = 0;
        for (long id: recs) {
            n += 1;
            if (good.contains(id)) {
                // it is good
                ngood += 1;
                // add to MAP sum
                sum += ngood / n;
            }
        }

        double aveP = ngood > 0 ? sum / ngood : 0;

        UserResult result = new UserResult(aveP);
        context.addUser(result);
        return result.withSuffix(suffix);
    }

    public static class UserResult extends TypedMetricResult {
        @MetricColumn("AvgPrec")
        public final double avgPrecision;

        public UserResult(double aveP) {
            avgPrecision = aveP;
        }
    }

    public static class AggregateResult extends TypedMetricResult {
        /**
         * The MAP over all users.  Users for whom no good items are included, and have a reciprocal
         * rank of 0.
         */
        @MetricColumn("MAP")
        public final double map;

        public AggregateResult(Context accum) {
            this.map = accum.allMean.getResult();
        }
    }

    public static class Context {
        private final LongSet universe;
        private final RecommenderEngine recommenderEngine;
        private final Mean allMean = new Mean();

        Context(LongSet universe, RecommenderEngine engine) {
            this.universe = universe;
            recommenderEngine = engine;
        }

        synchronized void addUser(UserResult ur) {
            allMean.increment(ur.avgPrecision);
        }
    }
}
