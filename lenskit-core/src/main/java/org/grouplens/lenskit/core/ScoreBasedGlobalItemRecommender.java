/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
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

public class ScoreBasedGlobalItemRecommender extends AbstractGlobalItemRecommender{

	protected final GlobalItemScorer scorer;
	
	public ScoreBasedGlobalItemRecommender(DataAccessObject dao, GlobalItemScorer scorer) {
	    super(dao);
	    this.scorer = scorer;
	}
	
    /**
     * Implement the ID-based recommendation in terms of the scorer. This method
     * uses {@link #getDefaultExcludes(long)} to supply a missing exclude set.
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
     * Get the default exclude set for a item in the global recommendation.  The base implementation returns
     * the input set.
     *
     * @param item The item to make recommendation
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
     * @param item The ID of the item.
     * @return All items for which predictions can be generated for the user.
     */
    protected LongSet getPredictableItems(LongSet items) {
        return Cursors.makeSet(dao.getItems());
    }

    

    /**
     * Pick the top <var>n</var> items from a score vector.
     *
     * @param n The number of items to recommend.
     * @param scores The scored item vector.
     * @return The top <var>n</var> items from <var>scores</var>, in descending
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
        for (Long2DoubleMap.Entry pred: scores.fast()) {
            final double v = pred.getDoubleValue();
            accum.put(pred.getLongKey(), v);
        }

        return accum.finish();
    }

}
