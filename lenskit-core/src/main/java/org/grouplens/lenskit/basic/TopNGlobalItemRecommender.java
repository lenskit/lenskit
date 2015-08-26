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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.util.collections.LongUtils;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * A global item recommender that recommends the top N items from a scorer.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class TopNGlobalItemRecommender extends AbstractGlobalItemRecommender {
    protected final ItemDAO itemDAO;
    protected final GlobalItemScorer scorer;

    @Inject
    public TopNGlobalItemRecommender(ItemDAO idao, GlobalItemScorer scorer) {
        itemDAO = idao;
        this.scorer = scorer;
    }

    /**
     * Implement the ID-based recommendation in terms of the scorer. This method
     * uses {@link #getDefaultExcludes(LongSet)} to supply a missing exclude set.
     */
    @Override
    protected List<ScoredId> globalRecommend(LongSet items, int n, LongSet candidates, LongSet exclude) {
        if (candidates == null) {
            candidates = itemDAO.getItemIds();
        }
        if (exclude == null) {
            exclude = getDefaultExcludes(items);
        }
        if (!exclude.isEmpty()) {
            candidates = LongUtils.setDifference(candidates, exclude);
        }

        SparseVector scores = scorer.globalScore(items, candidates);
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
    protected List<ScoredId> recommend(int n, SparseVector scores) {
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        if (n < 0) {
            n = scores.size();
        }

        ScoredItemAccumulator accum = new TopNScoredItemAccumulator(n);
        for (VectorEntry pred : scores) {
            final double v = pred.getValue();
            accum.put(pred.getKey(), v);
        }

        return accum.finish();
    }
}
