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
package org.grouplens.lenskit.eval.metrics.topn;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.ResultColumn;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.statistics.MeanAccumulator;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Compute the mean average precision.
 * <p>
 * The algorithm computed here is equivalent to <a href="https://github.com/benhamner/Metrics/blob/master/Python/ml_metrics/average_precision.py">Ben Hammer's Python implementation</a>,
 * as referenced by Kaggle.
 * </p>
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.2
 */
public class MAPTopNMetric extends AbstractMetric<MAPTopNMetric.Context, MAPTopNMetric.AggregateResult, MAPTopNMetric.UserResult> {
    private static final Logger logger = LoggerFactory.getLogger(MAPTopNMetric.class);

    private final String prefix;
    private final String suffix;
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ItemSelector goodItems;

    /**
     * Construct a new mean average precision top n metric
     * @param pre the prefix label for this evaluation, or {@code null} for no prefix.
     * @param sfx the suffix label for this evaluation, or {@code null} for no suffix.
     * @param listSize The number of recommendations to fetch.
     * @param candidates The candidate selector, provides a list of items which can be recommended
     * @param exclude The exclude selector, provides a list of items which must not be recommended
     *                (These items are removed from the candidate items to form the final candidate set)
     * @param goodItems The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     */
    public MAPTopNMetric(String pre, String sfx, int listSize, ItemSelector candidates, ItemSelector exclude, ItemSelector goodItems) {
        super(AggregateResult.class, UserResult.class);
        prefix = pre;
        suffix = sfx;
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        this.goodItems = goodItems;
    }

    @Override
    protected String getPrefix() {
        return prefix;
    }

    @Override
    protected String getSuffix() {
        return suffix;
    }

    @Override
    public Context createContext(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Context(ds.getTestData().getItemDAO().getItemIds());
    }

    @Override
    public UserResult doMeasureUser(TestUser user, Context context) {
        LongSet good = goodItems.select(user);
        if (good.isEmpty()) {
            logger.warn("no good items for user {}", user.getUserId());
            return new UserResult(0, false);
        }

        List<ScoredId> recs = user.getRecommendations(listSize, candidates, exclude);
        if (recs == null || recs.isEmpty()) {
            return null;
        }

        int n = 0;
        double ngood = 0;
        double sum = 0;
        for(ScoredId s : recs) {
            n += 1;
            if(good.contains(s.getId())) {
                // it is good
                ngood += 1;
                // add to MAP sum
                sum += ngood / n;
            }
        }

        UserResult result = new UserResult(sum / good.size(), ngood > 0);
        context.addUser(result);
        return result;
    }

    @Override
    protected AggregateResult getTypedResults(Context context) {
        return new AggregateResult(context);
    }

    public static class UserResult {
        @ResultColumn("AvgPrec")
        public final double avgPrecision;
        private final boolean isGood;

        public UserResult(double aveP, boolean good) {
            avgPrecision = aveP;
            isGood = good;
        }
    }

    public static class AggregateResult {
        /**
         * The MAP over all users.  Users for whom no good items are included, and have a reciprocal
         * rank of 0.
         */
        @ResultColumn("MAP")
        public final double map;

        /**
         * The MAP over those users for whom a good item could be recommended.
         */
        @ResultColumn("MAP.OfGood")
        public final double goodMAP;

        public AggregateResult(Context accum) {
            this.map = accum.allMean.getMean();
            this.goodMAP = accum.goodMean.getMean();
        }
    }

    public static class Context {
        private final LongSet universe;
        private final MeanAccumulator allMean = new MeanAccumulator();
        private final MeanAccumulator goodMean = new MeanAccumulator();

        Context(LongSet universe) {
            this.universe = universe;
        }

        void addUser(UserResult ur) {
            allMean.add(ur.avgPrecision);
            if (ur.isGood) {
                goodMean.add(ur.avgPrecision);
            }
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<MAPTopNMetric>{
        private ItemSelector goodItems = ItemSelectors.testRatingMatches(Matchers.greaterThanOrEqualTo(4.0d));

        public Builder() {
            // override the default candidate items with a more reasonable set.
            setCandidates(ItemSelectors.allItems());
        }

        public ItemSelector getGoodItems() {
            return goodItems;
        }

        /**
         * Set the set of items that will be considered &lsquo;good&rsquo; by the evaluation.
         *
         * @param goodItems A selector for good items.
         */
        public void setGoodItems(ItemSelector goodItems) {
            this.goodItems = goodItems;
        }

        @Override
        public MAPTopNMetric build() {
            return new MAPTopNMetric(prefix, suffix, listSize, candidates, exclude, goodItems);
        }
    }

}
