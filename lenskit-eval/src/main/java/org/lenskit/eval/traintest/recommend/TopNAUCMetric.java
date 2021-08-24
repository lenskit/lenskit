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
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
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
import java.util.BitSet;
import java.util.function.LongPredicate;
import java.util.stream.IntStream;

/**
 * Compute the area under the ROC curve.
 *
 * This metric is registered under the name `auc`.  It has two configuration parameters:
 *
 * `suffix`
 * :   a suffix to append to the column name
 *
 * `goodItems`
 * :   an item selector expression. The default is the user's test items.
 */
public class TopNAUCMetric extends ListOnlyTopNMetric<TopNAUCMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TopNAUCMetric.class);

    private final String suffix;
    private final ItemSelector goodItems;

    /**
     * Construct a new AUC metric with the user's test items as good.
     */
    public TopNAUCMetric() {
        this(ItemSelector.userTestItems(), null);
    }

    /**
     * Create a metric from a spec.
     * @param spec The specification.
     */
    @JsonCreator
    public TopNAUCMetric(PRMetricSpec spec) {
        this(ItemSelector.compileSelector(StringUtils.defaultString(spec.getGoodItems(), "user.testItems")),
             spec.getSuffix());
    }

    /**
     * Construct a new AUC top-N metric
     * @param sfx the suffix label for this evaluation, or {@code null} for no suffix.
     * @param good The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     */
    public TopNAUCMetric(ItemSelector good, String sfx) {
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
        for (long item: recs) {
            n += 1;
            if (good.contains(item)) {
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

    /**
     * Compute the AUC from a (sorted) stream of integers of good entries.
     *
     * This is not implemented as a collector because that would be disturbingly difficult.
     *
     * @param goodPositions The list of good-item indexes (0-based).
     * @param total The total number of entries (including bad)
     * @param totalGood The total number of good entries (including ones that never appear)
     *
     * @return The AUC.
     */
    static double computeAUC(int[] goodPositions, int total, int totalGood) {
        // use number of bad items _we have_ as proxy for number of all bad items
        int nbad = total - goodPositions.length;
        double auc = 0;
        double thickness = 1.0 / totalGood;

        int goodSoFar = 0;

        // iterate through the set bits
        for (int pos: goodPositions) {
            // how many bad items to the left of me?
            double clowns = pos - goodSoFar;
            // fraction of clowns? the fpr
            double fpr = nbad > 0 ? clowns / nbad : 0;
            // we have a good item. this means we can increment the auc
            // each good item increments the AUC by a slab with width (1-fpr)
            // and thickness 1/ngood
            auc += thickness * (1 - fpr);
            // and increment good
            goodSoFar += 1;
        }

        return auc;
    }

    public static class UserResult extends TypedMetricResult {
        @MetricColumn("AUC")
        public final double auc;

        public UserResult(double auc) {
            this.auc = auc;
        }
    }

    public static class AggregateResult extends TypedMetricResult {
        /**
         * The average AUC over all users.  Users for whom no good items are included, and have an AUC of 0.
         */
        @MetricColumn("AvgAUC")
        public final double avgAUC;

        public AggregateResult(Context accum) {
            this.avgAUC = accum.allMean.getResult();
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
            allMean.increment(ur.auc);
        }
    }
}
