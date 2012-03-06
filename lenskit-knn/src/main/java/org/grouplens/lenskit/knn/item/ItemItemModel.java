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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.io.Serializable;

import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.params.meta.Built;
import org.grouplens.lenskit.params.meta.DefaultBuilder;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Item-item similarity model. It stores and makes available the similarities
 * between items.
 *
 * <p/>
 * These similarities are post-normalization, so code using them
 * should use the same normalizations used by the builder to make use of the
 * similarity scores.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Built
@DefaultBuilder(ItemItemModelBuilder.class)
public class ItemItemModel implements Serializable {
    private static final long serialVersionUID = -5986236982760043379L;

    private static final ScoredLongList EMPTY_LIST = new ScoredLongArrayList();

    private final Long2ObjectMap<ScoredLongList> similarityMatrix;
    private final LongSortedSet itemUniverse;

    /**
     * Construct a new item-item model.
     * @param universe The set of item IDs. This should be equal to the key set
     *                 of the matrix.
     * @param matrix The similarity matrix columns (maps item ID to column)
     */
    @Inject
    public ItemItemModel(LongSortedSet universe, Long2ObjectMap<ScoredLongList> matrix) {
        itemUniverse = universe;
        similarityMatrix = matrix;
    }

    /**
     * Get the set of all items in the model.
     * @return The set of item IDs for all items in the model.
     */
    public LongSortedSet getItemUniverse() {
        return itemUniverse;
    }

    /**
     * Get the neighbors of an item scored by similarity. This is the corresponding
     * <em>column</em> of the item-item similarity matrix.
     * @param item The item to get the neighborhood for.
     * @return The column of the similarity matrix. If the item is unknown, an empty
     * list is returned.
     */
    @Nonnull
    public ScoredLongList getNeighbors(long item) {
        ScoredLongList nbrs = similarityMatrix.get(item);
        if (nbrs == null) {
            nbrs = EMPTY_LIST;
        }
        return nbrs;

    }
    
    /**
     * Compute item scores for a user.
     * 
     * @param userData The user vector for which scores are to be computed.
     * @param items The items to score.
     * @param scorer The neighborhood scorer used to score items.
     * @param neighborhoodSize The size of the neighborhood used to compute the score
     * @return The scores for the items. The key domain contains all items; only
     *         those items with scores are set.
     */
    public MutableSparseVector scoreItems(SparseVector userData,
                                             LongSortedSet items,
                                             NeighborhoodScorer scorer,
                                             int neighborhoodSize) {
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
            ScoredLongList neighbors = this.getNeighbors(item);

            if (neighbors == null) {
                /* we cannot predict this item */
                continue;
            }

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

            // compute score & place in vector
            final double score = scorer.score(neighbors, userData);
            if (!Double.isNaN(score)) {
                scores.set(item, score);
            }
        }

        return scores;
    }
}
