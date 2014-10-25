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

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.grouplens.lenskit.knn.MinNeighbors;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

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
        Predicate<ScoredId> usable = new VectorKeyPredicate(userData);

        // Create a channel for recording the neighborhoodsize
        MutableSparseVector sizeChannel = scores.getOrAddChannelVector(ItemItemScorer.NEIGHBORHOOD_SIZE_SYMBOL);
        sizeChannel.fill(0);
        // for each item, compute its prediction
        for (VectorEntry e : scores.view(VectorEntry.State.EITHER)) {
            final long item = e.getKey();

            // find all potential neighbors
            FluentIterable<ScoredId> nbrIter = FluentIterable.from(model.getNeighbors(item))
                                                             .filter(usable);
            if (neighborhoodSize > 0) {
                nbrIter = nbrIter.limit(neighborhoodSize);
            }
            List<ScoredId> neighbors = nbrIter.toList();

            // compute score & place in vector
            ScoredId score = null;

            if (neighbors.size() >= minNeighbors) {
                score = scorer.score(item, neighbors, userData);
            }

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

    private static class VectorKeyPredicate implements Predicate<ScoredId> {
        private final SparseVector vector;

        public VectorKeyPredicate(SparseVector v) {
            vector = v;
        }
        @Override
        public boolean apply(@Nullable ScoredId input) {
            return input != null && vector.containsKey(input.getId());
        }
    }
}
