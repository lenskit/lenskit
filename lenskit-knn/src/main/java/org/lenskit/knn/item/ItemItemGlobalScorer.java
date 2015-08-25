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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.basic.AbstractGlobalItemScorer;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Score items based on the basket of items using an item-item CF model.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemItemGlobalScorer extends AbstractGlobalItemScorer {
    protected final ItemItemModel model;
    @Nonnull
    protected final
    NeighborhoodScorer scorer;
    private final int neighborhoodSize;

    @Inject
    public ItemItemGlobalScorer(ItemItemModel m, @NeighborhoodSize int nnbrs) {
        model = m;
        // The global item scorer use the SimilaritySumNeighborhoodScorer for the unary ratings
        this.scorer = new SimilaritySumNeighborhoodScorer();
        neighborhoodSize = nnbrs;
    }

    @Override
    public void globalScore(@Nonnull Collection<Long> queryItems,
                            @Nonnull MutableSparseVector output) {
        // create the unary rating for the items
        LongSortedSet qItems = LongUtils.packedSet(queryItems);
        Long2DoubleMap basket = MutableSparseVector.create(qItems, 1.0).asMap();

        output.clear();
        for (VectorEntry e: output.view(VectorEntry.State.EITHER)) {
            ItemItemResult result = scoreItem(basket, e.getKey());
            if (result != null) {
                output.set(e, result.getScore());
            }
        }
    }

    protected ItemItemResult scoreItem(Long2DoubleMap scores, long item) {
        SparseVector allNeighbors = model.getNeighbors(item);
        ScoredItemAccumulator acc = null;
        if (neighborhoodSize > 0) {
            // FIXME Abstract accumulator selection logic
            acc = new TopNScoredItemAccumulator(neighborhoodSize);
        } else {
            acc = new UnlimitedScoredItemAccumulator();
        }

        for (VectorEntry e: allNeighbors) {
            if (scores.containsKey(e.getKey())) {
                acc.put(e.getKey(), e.getValue());
            }
        }

        Long2DoubleMap neighborhood = acc.finishMap();
        return scorer.score(item, neighborhood, scores);
    }
}
