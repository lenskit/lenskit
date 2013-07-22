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
package org.grouplens.lenskit.basic;


import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Recommender that recommends the top N items by a scorer.
 * Implements all methods required by {@link AbstractItemRecommender}. The
 * default exclude set is all items rated by the user.
 *
 * <p>Recommendations are returned in descending order of score.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class TopNItemRecommender extends AbstractItemRecommender {
    protected final UserEventDAO userEventDAO;
    protected final ItemDAO itemDAO;
    protected final ItemScorer scorer;

    @Inject
    public TopNItemRecommender(UserEventDAO uedao, ItemDAO idao, ItemScorer scorer) {
        userEventDAO = uedao;
        itemDAO = idao;
        this.scorer = scorer;
    }
    
    public ItemScorer getScorer() {
        return scorer;
    }

    /**
     * Implement the ID-based recommendation in terms of the scorer. This method
     * uses {@link #getDefaultExcludes(long)} to supply a missing exclude set.
     */
    @Override
    protected ScoredLongList recommend(long user, int n, LongSet candidates, LongSet exclude) {
        if (candidates == null) {
            candidates = getPredictableItems(user);
        }
        if (exclude == null) {
            exclude = getDefaultExcludes(user);
        }
        if (!exclude.isEmpty()) {
            candidates = LongSortedArraySet.setDifference(candidates, exclude);
        }

        SparseVector scores = scorer.score(user, candidates);
        return recommend(n, scores);
    }

    /**
     * Pick the top {@var n} items from a score vector.
     *
     * @param n      The number of items to recommend.
     * @param scores The scored item vector.
     * @return The top {@var n} items from {@var scores}, in descending
     *         order of score.
     */
    protected ScoredLongList recommend(int n, SparseVector scores) {
        if (scores.isEmpty()) {
            return new ScoredLongArrayList();
        }

        if (n < 0) {
            n = scores.size();
        }

        ScoredItemAccumulator accum = new TopNScoredItemAccumulator(n);
        for (VectorEntry pred : scores.fast()) {
            final double v = pred.getValue();
            accum.put(pred.getKey(), v);
        }

        return new ScoredLongArrayList(accum.finish());
    }

    /**
     * Get the default exclude set for a user.  The base implementation gets
     * all their rated items.
     *
     * @param user The user ID.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(long user) {
        return getDefaultExcludes(userEventDAO.getEventsForUser(user));
    }

    /**
     * Get the default exclude set for a user.  The base implementation returns
     * all their rated items.
     *
     * @param user The user history.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(UserHistory<? extends Event> user) {
        LongSet excludes = new LongOpenHashSet();
        for (Rating r : Iterables.filter(user, Rating.class)) {
            excludes.add(r.getItemId());
        }
        return excludes;
    }

    /**
     * Determine the items for which predictions can be made for a certain user.
     * This implementation is naive and asks the DAO for all items; subclasses
     * should override it with something more efficient if practical.
     *
     * @param user The user's ID.
     * @return All items for which predictions can be generated for the user.
     */
    protected LongSet getPredictableItems(long user) {
        return itemDAO.getItemIds();
    }

    /**
     * An intelligent provider for Top-N recommenders. It provides a Top-N recommender
     * if there is an {@link ItemScorer} available, and returns {@code null} otherwise.  This is
     * the default provider for {@link ItemRecommender}.
     */
    public static class Provider implements javax.inject.Provider<TopNItemRecommender> {
        private final UserEventDAO userEventDAO;
        private final ItemDAO itemDAO;
        private final ItemScorer scorer;

        @Inject
        public Provider(UserEventDAO uedao, ItemDAO idao,
                        @Nullable ItemScorer s) {
            userEventDAO = uedao;
            itemDAO = idao;
            scorer = s;
        }

        @Override
        public TopNItemRecommender get() {
            if (scorer == null) {
                return null;
            } else {
                return new TopNItemRecommender(userEventDAO, itemDAO, scorer);
            }
        }
    }
}
