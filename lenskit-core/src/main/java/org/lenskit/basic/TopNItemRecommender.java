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
package org.lenskit.basic;


import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.lenskit.data.events.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private static final Logger logger = LoggerFactory.getLogger(TopNItemRecommender.class);
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
     * Implement recommendation by calling {@link ItemScorer#score(long, Collection)} and sorting
     * the results by score.  This method uses {@link #getDefaultExcludes(long)} to get the default
     * exclude set for the user, if none is provided.
     */
    @Override
    protected List<Long> recommend(long user, int n, LongSet candidates, LongSet exclude) {
        candidates = getEffectiveCandidates(user, candidates, exclude);
        logger.debug("Computing {} recommendations for user {} from {} candidates",
                     n, user, candidates.size());

        // FIXME Make this more efficient - don't allocate extra BasicResults
        Map<Long, Double> scores = scorer.score(user, candidates);
        Iterable<Result> res = Iterables.transform(scores.entrySet(), Results.fromEntryFunction());
        return getTopNResults(n, res).idList();
    }

    /**
     * Implement recommendation by calling {@link ItemScorer#scoreWithDetails(long, Collection)} and sorting
     * the results. This method uses {@link #getDefaultExcludes(long)} to get the default
     * exclude set for the user, if none is provided.
     */
    @Override
    protected ResultList recommendWithDetails(long user, int n, LongSet candidates, LongSet exclude) {
        candidates = getEffectiveCandidates(user, candidates, exclude);
        logger.debug("Computing {} recommendations for user {} from {} candidates",
                     n, user, candidates.size());

        ResultMap scores = scorer.scoreWithDetails(user, candidates);
        return getTopNResults(n, scores);
    }

    private LongSet getEffectiveCandidates(long user, LongSet candidates, LongSet exclude) {
        if (candidates == null) {
            candidates = getPredictableItems(user);
        }
        if (exclude == null) {
            exclude = getDefaultExcludes(user);
        }
        if (!exclude.isEmpty()) {
            candidates = LongUtils.setDifference(candidates, exclude);
        }
        return candidates;
    }

    @Nonnull
    private ResultList getTopNResults(int n, Iterable<Result> scores) {
        Ordering<Result> ord = Results.scoreOrder();
        List<Result> topN;
        if (n < 0) {
            topN = ord.reverse().immutableSortedCopy(scores);
        } else {
            topN = ord.greatestOf(scores, n);
        }
        return Results.newResultList(topN);
    }

    /**
     * Get the default exclude set for a user.  The base implementation gets
     * all the items they have interacted with.
     *
     * @param user The user ID.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(long user) {
        return getDefaultExcludes(userEventDAO.getEventsForUser(user));
    }

    /**
     * Get the default exclude set for a user.  The base implementation returns
     * all the items they have interacted with (from {@link UserHistory#itemSet()}).
     *
     * @param user The user history.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(@Nullable UserHistory<? extends Event> user) {
        if (user == null) {
            return LongSets.EMPTY_SET;
        } else {
            return user.itemSet();
        }
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
}
