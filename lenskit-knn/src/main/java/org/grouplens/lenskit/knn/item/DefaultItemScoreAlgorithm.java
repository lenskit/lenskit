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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.knn.MinNeighbors;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Default item scoring algorithm. It uses up to {@link NeighborhoodSize} neighbors to
 * score each item.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DefaultItemScoreAlgorithm implements ItemScoreAlgorithm {
    private static Logger logger = LoggerFactory.getLogger(DefaultItemScoreAlgorithm.class);

    private final int neighborhoodSize;
    private final int minNeighbors;

    @Inject
    public DefaultItemScoreAlgorithm(@NeighborhoodSize int n, @MinNeighbors int min) {
        neighborhoodSize = n;
        minNeighbors = min <= 0 ? 1 : min;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void scoreItems(ItemItemModel model, SparseVector userData,
                           MutableSparseVector scores,
                           NeighborhoodScorer scorer) {
        MutableSparseVector neighbors = userData.mutableCopy();
        ScoredItemAccumulator acc = null;
        if (neighborhoodSize > 0) {
            acc = new TopNScoredItemAccumulator(neighborhoodSize);
        }

        // Create a channel for recording the neighborhoodsize
        MutableSparseVector sizeChannel = scores.getOrAddChannelVector(ItemItemScorer.NEIGHBORHOOD_SIZE_SYMBOL);
        sizeChannel.fill(0);
        // for each item, compute its prediction
        for (VectorEntry e : scores.view(VectorEntry.State.EITHER)) {
            final long item = e.getKey();

            neighbors.clear();
            // copy the neighbor vector into our work one (efficiently)
            neighbors.set(model.getNeighbors(item));

            if (neighbors.size() < minNeighbors) {
                continue;
            }

            if (acc != null && neighbors.size() < neighborhoodSize) {
                // compact the neighbors
                for (VectorEntry ne: neighbors) {
                    acc.put(ne.getKey(), ne.getValue());
                }
                LongSet set = acc.finishSet();
                // only keep the top N vectors
                neighbors.keySet().retainAll(set);
            }

            // compute score & place in vector
            ScoredId score = scorer.score(item, neighbors, userData);

            if (score != null) {
                scores.set(e, score.getScore());
                // FIXME Scorers should not need to do this.
                for (TypedSymbol sym: score.getChannelSymbols()) {
                    scores.getOrAddChannel(sym).put(e.getKey(), score.getChannelValue(sym));
                }
            }

            sizeChannel.set(e, neighbors.size());
        }
    }
}
