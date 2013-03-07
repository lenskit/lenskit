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
package org.grouplens.lenskit.core;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.GlobalItemScorer;
import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

public class ScoreBasedGlobalItemRecommender extends AbstractGlobalItemRecommender {

    protected final GlobalItemScorer scorer;

    public ScoreBasedGlobalItemRecommender(DataAccessObject dao, GlobalItemScorer scorer) {
        super(dao);
        this.scorer = scorer;
    }

    /**
     * Implement the ID-based recommendation in terms of the scorer. This method
     * uses {@link #getDefaultExcludes(LongSet)} to supply a missing exclude set.
     */
    @Override
    protected ScoredLongList globalRecommend(LongSet items, int n, LongSet candidates, LongSet exclude) {
        if (candidates == null) {
            candidates = getPredictableItems(items);
        }
        if (exclude == null) {
            exclude = getDefaultExcludes(items);
        }
        if (!exclude.isEmpty()) {
            candidates = LongSortedArraySet.setDifference(candidates, exclude);
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
     * Determine the items for which predictions can be made for a certain item.
     * This implementation is naive and asks the DAO for all items; subclasses
     * should override it with something more efficient if practical.
     *
     * @param items IDs of the basket items.
     * @return All items for which predictions can be generated for the user.
     */
    protected LongSet getPredictableItems(LongSet items) {
        return Cursors.makeSet(dao.getItems());
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

}
