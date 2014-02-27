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
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractTestUserMetric;
import org.grouplens.lenskit.eval.metrics.TestUserMetricAccumulator;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Metric that measures the entropy of the top N recommendations across all users.
 * 
 * This tell us essentially how large of a range of the items your recommender is covering.
 * 
 * Small values indicate that the algorithm tends to prefer a small number of items which it recomments
 * to all users. Large values mean that the algorithm recommends many different items (to many different 
 * users) 
 * 
 * The smallest value happens when the topN list is the same for all users (which would give an entropy
 * of roughly log_2(N)). The largest value happens when each item is recommended the same number of times
 * (for an entropy of roughly log_2(number of items)).
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNEntropyMetric extends AbstractTestUserMetric {
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    public TopNEntropyMetric(String lbl, int listSize, ItemSelector candidates, ItemSelector exclude) {
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        columns = ImmutableList.of(lbl);
    }
    
    @Override
    public Accum makeAccumulator(Attributed algo, TTDataSet ds) {
        return new Accum();
    }

    @Override
    public List<String> getColumnLabels() {
        return columns;
    }

    @Override
    public List<String> getUserColumnLabels() {
        return Collections.emptyList();
    }

    class Accum implements TestUserMetricAccumulator {
        Long2IntMap counts = new Long2IntOpenHashMap();
        int n = 0;
        
        @Nonnull
        @Override
        public List<Object> evaluate(TestUser user) {
            
            List<ScoredId> recs;
            recs = user.getRecommendations(listSize, candidates, exclude);
            if (recs == null) {
                return userRow();
            }
            
            for (ScoredId s: recs) {
                counts.put(s.getId(), counts.get(s.getId()) +1);
                n +=1;
            }
            return userRow();
        }

        @Nonnull
        @Override
        public List<Object> finalResults() {
            if (n>0) {
                double entropy = 0;
                for (Long2IntMap.Entry e : counts.long2IntEntrySet()) {
                    double p = (double) e.getIntValue()/n;
                    entropy -= p*Math.log(p)/Math.log(2);
                }
                return finalRow(entropy);
            } else {
                return finalRow();
            }
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, TopNEntropyMetric> {
        private String label = "TopN.pop.entropy";

        /**
         * Get the column label for this metric.
         * @return The column label.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Set the column label for this metric.
         * @param l The column label
         * @return The builder (for chaining).
         */
        public Builder setLabel(String l) {
            Preconditions.checkNotNull(l, "label cannot be null");
            label = l;
            return this;
        }

        @Override
        public TopNEntropyMetric build() {
            return new TopNEntropyMetric(label, listSize, candidates, exclude);
        }
    }

}
