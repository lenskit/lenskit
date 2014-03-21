/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A metric to compute the precision and recall of a recommender given a 
 * set of candidate items to recommend from and a set of desired items.
 * 
 * This can be used to compute metrics like fallout (probability that a 
 * recommendation is bad) by configuring bad items as the test item set.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNRecallPrecisionMetric extends AbstractTestUserMetric {
    private static final Logger logger = LoggerFactory.getLogger(TopNRecallPrecisionMetric.class);

    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;
    private final ItemSelector testItems;

    /**
     * Construct a new recall and precision top n metric
     * @param listSize The number of recommendations to fetch.
     * @param candidates The candidate selector, provides a list of items which can be recommended
     * @param exclude The exclude selector, provides a list of items which must not be recommended 
     *                (These items are removed from the candidate items to form the final candidate set)
     * @param testItems The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     */
    public TopNRecallPrecisionMetric(String[] lbls, int listSize, ItemSelector candidates, ItemSelector exclude, ItemSelector testItems) {
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        this.testItems = testItems;
        columns = ImmutableList.copyOf(lbls);
    }

    @Override
    public Accum makeAccumulator(Attributed algo, TTDataSet ds) {
        return new Accum(ds.getTestData().getItemDAO().getItemIds());
    }

    @Override
    public List<String> getColumnLabels() {
        return columns;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return columns;
    }

    class Accum implements TestUserMetricAccumulator {
        private final LongSet universe;

        double totalPrecision = 0;
        double totalRecall = 0;
        int nusers = 0;

        Accum(LongSet universe) {
            this.universe = universe;
        }

        @Nonnull
        @Override
        public List<Object> evaluate(TestUser user) {
            int tp = 0;
            int fp = 0;

            LongSet items = testItems.select(user.getTrainHistory(), user.getTestHistory(), universe);
            
            List<ScoredId> recs = user.getRecommendations(listSize, candidates, exclude);
            for(ScoredId s : recs) {
                if(items.contains(s.getId())) {
                    tp += 1; 
                } else {
                    fp += 1;
                }
            }
            int fn = items.size() - tp;
            
            if (items.size() > 0 && recs.size() > 0) {
                // if both the items set and recommendations are non-empty (no division by 0).
                double precision = (double) tp/(tp+fp);
                double recall = (double) tp/(tp+fn);
                totalPrecision += precision;
                totalRecall += recall;
                nusers += 1;
                return userRow(precision, recall);
            } else {
                return userRow();
            }
        }

        @Nonnull
        @Override
        public List<Object> finalResults() {
            if (nusers > 0) {
                return finalRow(totalPrecision / nusers, totalRecall / nusers);
            } else {
                return finalRow();
            }
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, TopNRecallPrecisionMetric>{
        private String[] labels = {"TopN.precision","TopN.recall"};
        private ItemSelector testItems = ItemSelectors.testRatingMatches(Matchers.greaterThanOrEqualTo(5.0d));

        public Builder() {
            // override the default candidate items with a more reasonable set.
            setCandidates(ItemSelectors.allItems());
        }

        /**
         * Get the column labels for this metric.
         * There are two computed metrics, precision and recall, listed in that order.
         * @return The column label.
         */
        public String[] getLabels() {
            return labels;
        }

        /**
         * Set the column label for this metric.
         * @param precision The column label for the precision metric.
         * @param recall the column label for the recall metric.
         * @return The builder (for chaining).
         */
        public Builder setLabels(String precision, String recall) {
            Preconditions.checkNotNull(precision, "label cannot be null");
            Preconditions.checkNotNull(recall, "label cannot be null");
            labels = new String[]{precision, recall};
            return this;
        }

        public ItemSelector getTestItems() {
            return testItems;
        }

        public Builder setTestItems(ItemSelector testItems) {
            this.testItems = testItems;
            return this;
        }

        @Override
        public TopNRecallPrecisionMetric build() {
            return new TopNRecallPrecisionMetric(labels, listSize, candidates, exclude, testItems);
        }
    }

}
