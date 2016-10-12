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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemBasedItemScorer;
import org.lenskit.api.ResultList;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Set;

/**
 * A global item recommender that recommends the top N items from a scorer.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class TopNItemBasedItemRecommender extends AbstractItemBasedItemRecommender {
    protected final ItemDAO itemDAO;
    protected final ItemBasedItemScorer scorer;

    @Inject
    public TopNItemBasedItemRecommender(ItemDAO idao, ItemBasedItemScorer scorer) {
        itemDAO = idao;
        this.scorer = scorer;
    }

    @Override
    public ResultList recommendRelatedItemsWithDetails(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        if (candidates == null) {
            candidates = itemDAO.getItemIds();
        }
        if (exclude == null) {
            exclude = getDefaultExcludes(LongUtils.asLongSet(basket));
        }
        if (!exclude.isEmpty()) {
            candidates = LongUtils.setDifference(LongUtils.asLongSet(candidates),
                                                 LongUtils.asLongSet(exclude));
        }

        ResultMap scores = scorer.scoreRelatedItemsWithDetails(basket, candidates);
        return recommend(n, scores);
    }

    /**
     * Get the default exclude set for a item in the global recommendation.
     * The base implementation returns the input set.
     *
     * @param items The items for which we are recommending.
     * @return The set of items to exclude.
     */
    protected LongSet getDefaultExcludes(LongSet items) {
        return items;
    }

    /**
     * Pick the top <var>n</var> items from a score vector.
     *
     * @param n      The number of items to recommend.
     * @param scores The scored item vector.
     * @return The top <var>n</var> items from <var>scores</var>, in descending
     *         order of score.
     */
    protected ResultList recommend(int n, ResultMap scores) {
        if (scores.isEmpty()) {
            Results.newResultList();
        }

        if (n < 0) {
            n = scores.size();
        }

        return Results.newResultList(Results.scoreOrder().greatestOf(scores, n));
    }
}
