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

import com.fasterxml.jackson.annotation.JsonCreator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
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
    public MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recs, Context context) {
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
        LongIterator iter = recs.iterator();
        while (iter.hasNext()) {
            n += 1;
            if(good.contains(iter.nextLong())) {
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
            this.map = accum.allMean.getMean();
        }
    }

    public static class Context {
        private final LongSet universe;
        private final RecommenderEngine recommenderEngine;
        private final MeanAccumulator allMean = new MeanAccumulator();

        Context(LongSet universe, RecommenderEngine engine) {
            this.universe = universe;
            recommenderEngine = engine;
        }

        void addUser(UserResult ur) {
            allMean.add(ur.avgPrecision);
        }
    }
}
