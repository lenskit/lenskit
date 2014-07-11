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

import it.unimi.dsi.fastutil.longs.LongIterator;
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

import java.util.List;

/**
 * An alternate methodology from computing precision/recall scores.
 * 
 * This is computed for each true positive item selected by the testItems selector.
 * For each item a set of other items is chosen by the candidate set. A topN 
 * recommendation is then made over the candidate items (plus the test item). The item is considered a 
 * hit if the true positive item is in the recommendations. The hit rate over the set 
 * of testItems is reported.
 * 
 * Under this methodology percussion and recall are roughly equivalent, so only recall 
 * (hit rate) is returned.
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class IndependentRecallTopNMetric extends AbstractMetric<IndependentRecallTopNMetric.Context, IndependentRecallTopNMetric.Result, IndependentRecallTopNMetric.Result> {
    private final String prefix;
    private final String suffix;
    private final int listSize;
    private final ItemSelector queryItems;
    private final ItemSelector candidates;
    private final ItemSelector exclude;

    /**
     * @param pre the prefix label for this evaluation, or {@code null} for no prefix.
     * @param sfx the suffix label for this evaluation, or {@code null} for no suffix.
     * @param queryItems the "true positive" items that we compute the hit rate over
     * @param candidates items to add to the recommendation, should be a random selection
     * @param listSize The size of the recommendation list to evaluate
     * @param exclude Items which should not be included in the recommendations.
     *                Should not include test set.
     */
    public IndependentRecallTopNMetric(String pre, String sfx, ItemSelector queryItems, ItemSelector candidates, int listSize, ItemSelector exclude) {
        super(Result.class, Result.class);
        prefix = pre;
        suffix = sfx;
        this.queryItems = queryItems;
        this.candidates = candidates;
        this.listSize = listSize;
        this.exclude = exclude;
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
    public Result doMeasureUser(TestUser user, Context context) {
        double score = 0;

        LongSet items = queryItems.select(user);
        LongIterator it = items.iterator();
        while (it.hasNext()) {
            final long l = it.nextLong();
            ItemSelector finalCandidates = ItemSelectors.union(ItemSelectors.fixed(l), candidates);

            List<ScoredId> recs = user.getRecommendations(listSize, finalCandidates, exclude);
            for (ScoredId s : CollectionUtils.fast(recs)) {
                if (s.getId() == l) {
                    score +=1;
                }
            }
        }

        int n = items.size();
        if (n>0) {
            score /= n;
            context.mean.add(score);
            return new Result(score);
        } else {
            return null;
        }
    }

    @Override
    protected Result getTypedResults(Context context) {
        if (context.mean.getCount() > 0) {
            return new Result(context.mean.getMean());
        } else {
            return null;
        }
    }

    public static class Result {
        @ResultColumn("IndepRecall")
        public final double recall;

        public Result(double r) {
            recall = r;
        }
    }

    public class Context {
        private final LongSet universe;
        private final MeanAccumulator mean = new MeanAccumulator();
        
        Context(LongSet universe) {
            this.universe = universe;
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<IndependentRecallTopNMetric> {
        private ItemSelector goodItems = ItemSelectors.testItems();

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

        public IndependentRecallTopNMetric build() {
            return new IndependentRecallTopNMetric(prefix, suffix, goodItems, candidates, listSize, exclude);
        }
    }
}
