/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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


import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.results.ResultAccumulator;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.collections.UnlimitedLong2DoubleAccumulator;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
    protected final DataAccessObject dao;
    protected final ItemScorer scorer;

    @Inject
    public TopNItemRecommender(DataAccessObject data, ItemScorer scorer) {
        dao = data;
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

        Map<Long, Double> scores = scorer.score(user, candidates);
        Long2DoubleAccumulator accum;
        if (n >= 0) {
            accum = new TopNLong2DoubleAccumulator(n);
        } else {
            accum = new UnlimitedLong2DoubleAccumulator();
        }

        Long2DoubleMap map = LongUtils.asLong2DoubleMap(scores);

        for (Long2DoubleMap.Entry e: Vectors.fastEntries(map)) {
            accum.put(e.getLongKey(), e.getDoubleValue());
        }

        return accum.finishList();
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
        logger.debug("computing effective candidates for user {} from {} candidates and {} excluded items",
                     user, candidates.size(), exclude.size());
        if (!exclude.isEmpty()) {
            candidates = LongUtils.setDifference(candidates, exclude);
        }
        return candidates;
    }

    @Nonnull
    private ResultList getTopNResults(int n, Iterable<Result> scores) {
        ResultAccumulator accum = ResultAccumulator.create(n);
        for (Result r: scores) {
            accum.add(r);
        }
        return accum.finish();
    }

    /**
     * Get the default exclude set for a user.  The base implementation gets
     * all the items they have interacted with.
     *
     * @param user The user ID.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(long user) {
        // FIXME Support things other than ratings
        return dao.query(CommonTypes.RATING)
                  .withAttribute(CommonAttributes.USER_ID, user)
                  .valueSet(CommonAttributes.ITEM_ID);
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
        return dao.getEntityIds(CommonTypes.ITEM);
    }
}
