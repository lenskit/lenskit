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
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.metrics.MetricAccumulator;
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
public class PrecisionRecallTopNMetric extends AbstractMetric<PrecisionRecallTopNMetric.Accumulator> {
    private static final Logger logger = LoggerFactory.getLogger(PrecisionRecallTopNMetric.class);

    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;
    private final ItemSelector queryItems;

    /**
     * Construct a new recall and precision top n metric
     * @param listSize The number of recommendations to fetch.
     * @param candidates The candidate selector, provides a list of items which can be recommended
     * @param exclude The exclude selector, provides a list of items which must not be recommended 
     *                (These items are removed from the candidate items to form the final candidate set)
     * @param queryItems The list of items to consider "true positives", all other items will be treated
     *                  as "false positives".
     */
    public PrecisionRecallTopNMetric(String[] lbls, int listSize, ItemSelector candidates, ItemSelector exclude, ItemSelector queryItems) {
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        this.queryItems = queryItems;
        columns = ImmutableList.copyOf(lbls);
    }

    @Override
    public Accumulator createAccumulator(Attributed algo, TTDataSet ds, Recommender rec) {
        return new Accumulator(ds.getTestData().getItemDAO().getItemIds());
    }

    @Override
    public List<String> getColumnLabels() {
        return columns;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return columns;
    }

    @Nonnull
    @Override
    public List<Object> measureUser(TestUser user, Accumulator accumulator) {
        int tp = 0;
        int fp = 0;

        LongSet items = queryItems.select(user.getTrainHistory(),
                                          user.getTestHistory(),
                                          accumulator.universe);

        List<ScoredId> recs = user.getRecommendations(listSize, candidates, exclude);
        for(ScoredId s : CollectionUtils.fast(recs)) {
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
            accumulator.addUser(precision, recall);
            return userRow(precision, recall);
        } else {
            return userRow();
        }
    }

    public class Accumulator implements MetricAccumulator {
        private final LongSet universe;

        double totalPrecision = 0;
        double totalRecall = 0;
        int nusers = 0;

        Accumulator(LongSet universe) {
            this.universe = universe;
        }

        private void addUser(double prec, double rec) {
            totalPrecision += prec;
            totalRecall += rec;
            nusers += 1;
        }

        @Nonnull
        @Override
        public List<Object> finish() {
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
    public static class Builder extends TopNMetricBuilder<Builder, PrecisionRecallTopNMetric>{
        private String[] labels = {"TopN.Precision","TopN.Recall"};
        private ItemSelector queryItems = ItemSelectors.testRatingMatches(Matchers.greaterThanOrEqualTo(4.0d));

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

        public ItemSelector getQueryItems() {
            return queryItems;
        }

        public Builder setQueryItems(ItemSelector queryItems) {
            this.queryItems = queryItems;
            return this;
        }

        @Override
        public PrecisionRecallTopNMetric build() {
            return new PrecisionRecallTopNMetric(labels, listSize, candidates, exclude, queryItems);
        }
    }

}
