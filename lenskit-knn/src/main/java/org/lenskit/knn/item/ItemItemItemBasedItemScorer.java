/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.ScoredIdAccumulator;
import org.lenskit.util.TopNScoredIdAccumulator;
import org.lenskit.util.UnlimitedScoredIdAccumulator;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemBasedItemScorer;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Score items based on the basket of items using an item-item CF model.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ItemItemItemBasedItemScorer extends AbstractItemBasedItemScorer {
    protected final ItemItemModel model;
    @Nonnull
    protected final
    NeighborhoodScorer scorer;
    private final int neighborhoodSize;

    @Inject
    public ItemItemItemBasedItemScorer(ItemItemModel m, @NeighborhoodSize int nnbrs) {
        model = m;
        // The global item scorer use the SimilaritySumNeighborhoodScorer for the unary ratings
        this.scorer = new SimilaritySumNeighborhoodScorer();
        neighborhoodSize = nnbrs;
    }

    @Nonnull
    @Override
    public Map<Long, Double> scoreRelatedItems(@Nonnull Collection<Long> basket, @Nonnull Collection<Long> items) {
        Long2DoubleMap results = new Long2DoubleOpenHashMap();
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.basic(results);

        scoreItems(basket, items, accum);

        return results;
    }

    @Override
    public ResultMap scoreRelatedItemsWithDetails(@Nonnull Collection<Long> basket, Collection<Long> items) {
        List<ItemItemResult> results = new ArrayList<>();
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.detailed(results);

        scoreItems(basket, items, accum);

        return Results.newResultMap(results);
    }

    /**
     * Score items into an accumulator.
     * @param basket The basket of reference items.
     * @param items The item scores.
     * @param accum The accumulator.
     */
    private void scoreItems(@Nonnull Collection<Long> basket, Collection<Long> items, ItemItemScoreAccumulator accum) {
        LongSet bset = LongUtils.packedSet(basket);
        Long2DoubleMap basketScores = LongUtils.constantDoubleMap(bset, 1.0);

        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            scoreItem(basketScores, item, accum);
        }
    }

    /**
     * Score a single item into an accumulator.
     * @param scores The reference scores.
     * @param item The item to score.
     * @param accum The accumulator.
     */
    protected void scoreItem(Long2DoubleMap scores, long item, ItemItemScoreAccumulator accum) {
        Long2DoubleMap allNeighbors = model.getNeighbors(item);
        ScoredIdAccumulator acc;
        if (neighborhoodSize > 0) {
            // FIXME Abstract accumulator selection logic
            acc = new TopNScoredIdAccumulator(neighborhoodSize);
        } else {
            acc = new UnlimitedScoredIdAccumulator();
        }

        for (Long2DoubleMap.Entry nbr: allNeighbors.long2DoubleEntrySet()) {
            if (scores.containsKey(nbr.getLongKey())) {
                acc.put(nbr.getLongKey(), nbr.getDoubleValue());
            }
        }

        Long2DoubleMap neighborhood = acc.finishMap();
        scorer.score(item, neighborhood, scores, accum);
    }
}
