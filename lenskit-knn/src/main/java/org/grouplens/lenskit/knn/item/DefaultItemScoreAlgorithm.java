/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default item scoring algorithm. It uses up to {@link NeighborhoodSize} neighbors to
 * score each item.
 *
 * @author Michael Ekstrand
 */
public class DefaultItemScoreAlgorithm implements ItemScoreAlgorithm {
    private static Logger logger = LoggerFactory.getLogger(DefaultItemScoreAlgorithm.class);
    private int neighborhoodSize;

    @NeighborhoodSize
    public void setNeighborhoodSize(int n) {
        neighborhoodSize = n;
    }

    /**
     * Compute item scores.
     *
     * @param model The item-item similarity model to use.
     * @param userData The user vector for which scores are to be computed.
     * @param items The items to score.
     * @param scorer The neighborhood scorer used to score items.
     * @return The scores for the items. The key domain contains all items; only
     *         those items with scores are set.
     */
    @Override
    public MutableSparseVector scoreItems(ItemItemModel model,
                                          SparseVector userData, LongSortedSet items,
                                          NeighborhoodScorer scorer) {
        MutableSparseVector scores = new MutableSparseVector(items);
        // We ran reuse accumulators
        ScoredItemAccumulator accum;
        if (neighborhoodSize > 0) {
            accum = new TopNScoredItemAccumulator(neighborhoodSize);
        } else {
            accum = new UnlimitedScoredItemAccumulator();
        }

        // for each item, compute its prediction
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();

            // find all potential neighbors
            // FIXME: Take advantage of the fact that the neighborhood is sorted
            ScoredLongList neighbors = model.getNeighbors(item);
            final int nnbrs = neighbors.size();

            // filter and truncate the neighborhood
            ScoredLongListIterator niter = neighbors.iterator();
            while (niter.hasNext()) {
                long oi = niter.nextLong();
                double score = niter.getScore();
                if (userData.containsKey(oi)) {
                    accum.put(oi, score);
                }
            }
            neighbors = accum.finish();
            if (logger.isTraceEnabled()) { // conditional to avoid alloc
                logger.trace("using {} of {} neighbors for {}",
                             new Object[]{neighbors.size(), nnbrs, item});
            }

            // compute score & place in vector
            final double score = scorer.score(neighbors, userData);
            if (!Double.isNaN(score)) {
                scores.set(item, score);
            }
        }

        return scores;
    }
}
