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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.transform.normalize.VectorTransformation;

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
    public abstract void applyReversedTransform(VectorTransformation transform);

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
        public void applyReversedTransform(VectorTransformation transform) {
            MutableSparseVector vec = MutableSparseVector.create(receiver);
            transform.unapply(vec);
            receiver.putAll(vec.asMap());
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
        public void applyReversedTransform(VectorTransformation transform) {
            MutableSparseVector vec = MutableSparseVector.create(itemIds);
            for (ItemItemResult res: receiver) {
                vec.set(res.getId(), res.getScore());
            }
            transform.unapply(vec);
            int n = receiver.size();
            for (int i = 0; i < n; i++) {
                ItemItemResult res = receiver.get(i);
                receiver.set(i, res.rescore(vec.get(res.getId())));
            }
        }
    }
}
