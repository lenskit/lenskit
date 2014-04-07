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
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.AbstractMetric;
import org.grouplens.lenskit.eval.traintest.TestUser;
import org.grouplens.lenskit.scored.ScoredId;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Metric that measures how popular the items in the TopN list are.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TopNPopularityMetric extends AbstractMetric<TopNPopularityMetric.Accumulator> {
    private final int listSize;
    private final ItemSelector candidates;
    private final ItemSelector exclude;
    private final ImmutableList<String> columns;

    public TopNPopularityMetric(String lbl, int listSize, ItemSelector candidates, ItemSelector exclude) {
        this.listSize = listSize;
        this.candidates = candidates;
        this.exclude = exclude;
        columns = ImmutableList.of(lbl);
    }

    /**
     * Computes the popularity of a set of ratings as the number of users who have rated an item
     * This function is robust in the face of multiple ratings on the same item by the same user.
     * @return an immutable map from movie Ids to the number of users who have rated the identified movie.
     */
    private Long2IntMap computePop(EventDAO dao) {

        Long2ObjectOpenHashMap<LongSet> watchingUsers = new Long2ObjectOpenHashMap<LongSet>();
        for (Rating r : dao.streamEvents(Rating.class).fast()) {
            long item = r.getItemId();
            long user = r.getUserId();
            if (! watchingUsers.containsKey(item)) {
                watchingUsers.put(item, new LongOpenHashSet());
            }
            watchingUsers.get(item).add(user);
        }
        
        Long2IntMap userCounts = new Long2IntOpenHashMap();
        for (long item : watchingUsers.keySet()) {
            userCounts.put(item, watchingUsers.get(item).size());
        }
        return Long2IntMaps.unmodifiable(userCounts);
    }
    
    @Override
    public Accumulator createAccumulator(Attributed algo, TTDataSet ds, Recommender rec) {
        Long2IntMap popularity = computePop(ds.getTrainingDAO()); 
        return new Accumulator(popularity);
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
        List<ScoredId> recs;
        recs = user.getRecommendations(listSize, candidates, exclude);
        if (recs == null || recs.isEmpty()) {
            return userRow();
        }
        double pop = 0;
        for (ScoredId s : recs) {
            pop += accumulator.popularity.get(s.getId()); // default value should be 0 here.
        }
        pop = pop / recs.size();

        accumulator.addUserValue(pop);
        return userRow(pop);
    }

    public class Accumulator extends AbstractMetric.MeanAccumulator {
        Long2IntMap popularity;

        public Accumulator(Long2IntMap popularity) {
            this.popularity = popularity;
        }
    }

    /**
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    public static class Builder extends TopNMetricBuilder<Builder, TopNPopularityMetric> {
        private String label = "TopN.avgPop";

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
        public TopNPopularityMetric build() {
            return new TopNPopularityMetric(label, listSize, candidates, exclude);
        }
    }

}
