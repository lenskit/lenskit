/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import org.grouplens.lenskit.collections.CollectionUtils;
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
 * Compute the mean reciprocal rank.
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class MRRTopNMetric extends AbstractMetric<MRRTopNMetric.Context, MRRTopNMetric.AggregateResult, MRRTopNMetric.UserResult> {
    private static final Logger logger = LoggerFactory.getLogger(MRRTopNMetric.class);

    private final String prefix;
    private final String suffix;
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ItemSelector goodItems;

    /**
     * Construct a new recall and precision top n metric
     * @param pre the prefix label for this evaluation, or {@code null} for no prefix.
     * @param sfx the suffix label for this evaluation, or {@code null} for no suffix.
     * @param listSize The number of recommendations to fetch.
     * @param candidates The candidate selector, provides a list of items which can be recommended
     * @param exclude The exclude selector, provides a list of items which must not be recommended
     *                (These items are removed from the candidate items to form the final candidate set)
     * @param goodItems The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     */
    public MRRTopNMetric(String pre, String sfx, int listSize, ItemSelector candidates, ItemSelector exclude, ItemSelector goodItems) {
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
        }

        List<ScoredId> recs = user.getRecommendations(listSize, candidates, exclude);
        Integer rank = null;
        int i = 0;
        for(ScoredId s : CollectionUtils.fast(recs)) {
            i++;
            if(good.contains(s.getId())) {
                rank = i;
                break;
            }
        }

        UserResult result = new UserResult(rank);
        context.addUser(result);
        return result;
    }

    @Override
    protected AggregateResult getTypedResults(Context context) {
        return new AggregateResult(context);
    }

    public static class UserResult {
        @ResultColumn("Rank")
        public final Integer rank;

        public UserResult(Integer r) {
            rank = r;
        }

        @ResultColumn("RecipRank")
        public double getRecipRank() {
            return rank == null ? 0 : 1.0 / rank;
        }
    }

    public static class AggregateResult {
        /**
         * The MRR over all users.  Users for whom no good items are included, and have a reciprocal
         * rank of 0.
         */
        @ResultColumn("MRR")
        public final double mrr;
        /**
         * The MRR over those users for whom a good item could be recommended.
         */
        @ResultColumn("MRR.OfGood")
        public final double goodMRR;

        public AggregateResult(Context accum) {
            this.mrr = accum.allMean.getMean();
            this.goodMRR = accum.goodMean.getMean();
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
            allMean.add(ur.getRecipRank());
            if (ur.rank != null) {
                goodMean.add(ur.getRecipRank());
            }
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<MRRTopNMetric>{
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
        public MRRTopNMetric build() {
            return new MRRTopNMetric(prefix, suffix, listSize, candidates, exclude, goodItems);
        }
    }

}
