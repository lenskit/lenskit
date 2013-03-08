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
package org.grouplens.lenskit.knn.item;

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Default item scoring algorithm. It uses up to {@link NeighborhoodSize} neighbors to
 * score each item.
 *
 * @author Michael Ekstrand
 */
@Shareable
public class DefaultItemScoreAlgorithm implements ItemScoreAlgorithm, Serializable {
    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(DefaultItemScoreAlgorithm.class);
    private int neighborhoodSize;

    @Inject
    public DefaultItemScoreAlgorithm(@NeighborhoodSize int n) {
        neighborhoodSize = n;
    }

    @Override
    public void scoreItems(ItemItemModel model, SparseVector userData,
                           MutableSparseVector scores,
                           NeighborhoodScorer scorer) {
        // We ran reuse accumulators
        ScoredItemAccumulator accum;
        if (neighborhoodSize > 0) {
            accum = new TopNScoredItemAccumulator(neighborhoodSize);
        } else {
            accum = new UnlimitedScoredItemAccumulator();
        }

        // Create a channel for recording the neighborhoodsize
        scores.alwaysAddChannel(ItemItemScorer.NEIGHBORHOOD_SIZE_SYMBOL);
        // for each item, compute its prediction
        for (VectorEntry e : scores.fast(VectorEntry.State.EITHER)) {
            final long item = e.getKey();

            // find all potential neighbors
            List<ScoredId> neighbors = model.getNeighbors(item);
            final int nnbrs = neighbors.size();

            // filter and truncate the neighborhood
            Iterator<ScoredId> niter = neighbors.iterator();
            for (ScoredId neighbor : neighbors) {
                long oi = neighbor.getId();
                double score = neighbor.getScore();
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
            scores.channel(ItemItemScorer.NEIGHBORHOOD_SIZE_SYMBOL).
                    set(e.getKey(), neighbors.size()); // set size even if no score
            if (!Double.isNaN(score)) {
                scores.set(e, score);
            }
        }
    }
}
