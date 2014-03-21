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
import it.unimi.dsi.fastutil.longs.LongSortedSets;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
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
public class TopNIndependentRecallMetric extends AbstractTestUserMetric {
    private final int listSize;
    private final ItemSelector testItems;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    /**
     * @param lbl the label for the result column of this evaluation.
     * @param testItems the "true positive" items that we compute the hit rate over
     * @param candidates items to add to the recommendation, should be a random selection
     * @param listSize The size of the recommendation list to evaluate
     * @param exclude Items which should not be included in the recommendations.
     *                Should not include test set.
     */
    public TopNIndependentRecallMetric(String lbl, ItemSelector testItems, ItemSelector candidates, int listSize, ItemSelector exclude) {
        columns = ImmutableList.of(lbl);
        this.testItems = testItems;
        this.candidates = candidates;
        this.listSize = listSize;
        this.exclude = exclude;
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
        
        double total = 0;
        int nusers = 0;

        Accum(LongSet universe) {
            this.universe = universe;
        }

        @Nonnull
        @Override
        public List<Object> evaluate(TestUser user) {
            double score = 0;

            SingletonSelector theItem = new SingletonSelector();
            ItemSelector finalCandidates = ItemSelectors.union(candidates, theItem);
            
            LongSet items = testItems.select(user.getTrainHistory(), user.getTestHistory(), universe);
            for (final long l : items) {
                theItem.setTheItem(l);
                
                List<ScoredId> recs = user.getRecommendations(listSize, finalCandidates, exclude);
                for (ScoredId s : recs) {
                    if (s.getId() == l) {
                        score +=1;
                    }
                }
            }
            
            int n = items.size();
            if (n>0) {
                score /= n;
                total += score;
                nusers += 1;
                return userRow(score);
            } else {
                return userRow();
            }
        }

        @Nonnull
        @Override
        public List<Object> finalResults() {
            if (nusers > 0) {
                return finalRow(total / nusers);
            } else {
                return finalRow();
            }
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, TopNIndependentRecallMetric> {
        private String lbl = "TopN.Independent.Recall";
        private ItemSelector testItems = ItemSelectors.testItems();

        public String getLbl() {
            return lbl;
        }

        public Builder setLbl(String lbl) {
            this.lbl = lbl;
            return this;
        }
        public ItemSelector getTestItems() {
            return testItems;
        }

        public Builder setTestItems(ItemSelector testItems) {
            this.testItems = testItems;
            return this;
        }

        public TopNIndependentRecallMetric build() {
            return new TopNIndependentRecallMetric(lbl, testItems, candidates, listSize, exclude);
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
