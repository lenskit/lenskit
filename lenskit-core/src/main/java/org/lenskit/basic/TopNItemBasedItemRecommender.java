/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemBasedItemScorer;
import org.lenskit.api.ResultList;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
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
    protected final DataAccessObject dao;
    protected final ItemBasedItemScorer scorer;

    @Inject
    public TopNItemBasedItemRecommender(DataAccessObject data, ItemBasedItemScorer scorer) {
        dao = data;
        this.scorer = scorer;
    }

    @Override
    public ResultList recommendRelatedItemsWithDetails(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        if (candidates == null) {
            candidates = dao.getEntityIds(CommonTypes.ITEM);
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
