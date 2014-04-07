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

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSets;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;

import javax.annotation.Nonnull;
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
public class IndependentRecallTopNMetric extends AbstractMetric<IndependentRecallTopNMetric.Accumulator> {
    private final int listSize;
    private final ItemSelector queryItems;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    /**
     * @param lbl the label for the result column of this evaluation.
     * @param queryItems the "true positive" items that we compute the hit rate over
     * @param candidates items to add to the recommendation, should be a random selection
     * @param listSize The size of the recommendation list to evaluate
     * @param exclude Items which should not be included in the recommendations.
     *                Should not include test set.
     */
    public IndependentRecallTopNMetric(String lbl, ItemSelector queryItems, ItemSelector candidates, int listSize, ItemSelector exclude) {
        columns = ImmutableList.of(lbl);
        this.queryItems = queryItems;
        this.candidates = candidates;
        this.listSize = listSize;
        this.exclude = exclude;
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
        double score = 0;

        SingletonSelector theItem = new SingletonSelector();
        ItemSelector finalCandidates = ItemSelectors.union(candidates, theItem);

        LongSet items = queryItems.select(user.getTrainHistory(), user.getTestHistory(), accumulator.universe);
        LongIterator it = items.iterator();
        while (it.hasNext()) {
            final long l = it.nextLong();
            theItem.setTheItem(l);

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
            accumulator.addUserValue(score);
            return userRow(score);
        } else {
            return userRow();
        }
    }

    public class Accumulator extends AbstractMetric.MeanAccumulator {
        private final LongSet universe;
        
        Accumulator(LongSet universe) {
            this.universe = universe;
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, IndependentRecallTopNMetric> {
        private String lbl = "TopN.Independent.Recall";
        private ItemSelector queryItems = ItemSelectors.testItems();

        public String getLbl() {
            return lbl;
        }

        public Builder setLbl(String lbl) {
            this.lbl = lbl;
            return this;
        }
        public ItemSelector getQueryItems() {
            return queryItems;
        }

        public Builder setQueryItems(ItemSelector queryItems) {
            this.queryItems = queryItems;
            return this;
        }

        public IndependentRecallTopNMetric build() {
            return new IndependentRecallTopNMetric(lbl, queryItems, candidates, listSize, exclude);
        }
    }

    private class SingletonSelector implements ItemSelector {
        long theItem = 0;
        
        @Override
        public LongSet select(UserHistory<Event> trainingData, UserHistory<Event> testData, LongSet universe) {
            return LongSortedSets.singleton(theItem);
        }

        private void setTheItem(long theItem) {
            this.theItem = theItem;
        }
    }
}
