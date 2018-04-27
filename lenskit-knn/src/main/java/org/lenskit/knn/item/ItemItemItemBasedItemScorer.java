/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.collections.UnlimitedLong2DoubleAccumulator;
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
        Long2DoubleAccumulator acc;
        if (neighborhoodSize > 0) {
            // FIXME Abstract accumulator selection logic
            acc = new TopNLong2DoubleAccumulator(neighborhoodSize);
        } else {
            acc = new UnlimitedLong2DoubleAccumulator();
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
