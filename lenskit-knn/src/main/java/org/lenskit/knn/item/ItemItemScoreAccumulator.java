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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.results.Results;
import org.lenskit.util.InvertibleFunction;

import java.util.List;

/**
 * Score accumulator for item-item recommendation.  It is used to record the results of the item-item CF
 * scoring of one or more items.
 *
 * Accumulating results here allows us to avoid memory allocations when detailed results are not required.
 */
public abstract class ItemItemScoreAccumulator {
    private ItemItemScoreAccumulator() {}

    /**
     * Add a score to the accumulator.
     * @param item The item to score.
     * @param score The score.
     * @param nnbrs The number of neighbors used.
     * @param weight The total neighbor weight.
     */
    public abstract void add(long item, double score, int nnbrs, double weight);

    /**
     * Apply the reverse of a transform to the results.
     * @param transform The transform to apply.
     */
    public abstract void applyReversedTransform(InvertibleFunction<Long2DoubleMap, Long2DoubleMap> transform);

    /**
     * Construct an accumulator that will store items and scores in a map.
     * @param receiver The map to receive the results.
     * @return The accumulator.
     */
    static ItemItemScoreAccumulator basic(Long2DoubleMap receiver) {
        return new BasicAccumulator(receiver);
    }

    /**
     * Construct an accumulator that will store full detailed results in a map.
     * @param receiver The map to receive the results.
     * @return The accumulator.
     */
    static ItemItemScoreAccumulator detailed(List<ItemItemResult> receiver) {
        return new DetailedAccumulator(receiver);
    }

    private static class BasicAccumulator extends ItemItemScoreAccumulator {
        private final Long2DoubleMap receiver;

        BasicAccumulator(Long2DoubleMap recv) {
            receiver = recv;
        }

        @Override
        public void add(long item, double score, int nnbrs, double weight) {
            receiver.put(item, score);
        }

        @Override
        public void applyReversedTransform(InvertibleFunction<Long2DoubleMap, Long2DoubleMap> transform) {
            // TODO Make this in-place
            receiver.putAll(transform.unapply(receiver));
        }
    }

    private static class DetailedAccumulator extends ItemItemScoreAccumulator {
        private final LongSet itemIds = new LongOpenHashSet();
        private final List<ItemItemResult> receiver;

        DetailedAccumulator(List<ItemItemResult> recv) {
            receiver = recv;
            for (ItemItemResult res: recv) {
                itemIds.add(res.getId());
            }
        }

        @Override
        public void add(long item, double score, int nnbrs, double weight) {
            receiver.add(new ItemItemResult(item, score, nnbrs, weight));
            itemIds.add(item);
        }

        @Override
        public void applyReversedTransform(InvertibleFunction<Long2DoubleMap, Long2DoubleMap> transform) {
            Long2DoubleMap scores = transform.unapply(Results.newResultMap(receiver).scoreMap());

            int n = receiver.size();
            for (int i = 0; i < n; i++) {
                ItemItemResult res = receiver.get(i);
                receiver.set(i, res.rescore(scores.get(res.getId())));
            }
        }
    }
}
