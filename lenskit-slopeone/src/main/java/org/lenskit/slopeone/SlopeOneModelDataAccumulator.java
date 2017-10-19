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
package org.lenskit.slopeone;

import it.unimi.dsi.fastutil.longs.*;
import org.apache.commons.lang3.tuple.Pair;
import org.lenskit.util.keys.KeyedObjectMap;
import org.lenskit.util.keys.KeyedObjectMapBuilder;
import org.lenskit.util.keys.SortedKeyIndex;

import java.util.Map;

public class SlopeOneModelDataAccumulator {

    private Long2ObjectMap<Pair<Long2DoubleMap, Long2IntMap>> workMatrix;
    private double damping;

    /**
     * Creates an accumulator to process rating data and generate the necessary data for
     * a {@code SlopeOneItemScorer}.
     *
     * @param damping   A damping term for deviation calculations.
     * @param items     The set of known item IDs.
     */
    public SlopeOneModelDataAccumulator(double damping, LongSet items) {
        this.damping = damping;

        workMatrix = new Long2ObjectOpenHashMap<>(items.size());
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            workMatrix.put(item, Pair.<Long2DoubleMap, Long2IntMap>of(new Long2DoubleOpenHashMap(),
                                                                      new Long2IntOpenHashMap()));
        }
    }

    /**
     * Puts the item pair into the accumulator.
     *
     * @param id1      The id of the first item.
     * @param itemVec1 The rating vector of the first item.
     * @param id2      The id of the second item.
     * @param itemVec2 The rating vector of the second item.
     */
    public void putItemPair(long id1, Long2DoubleSortedMap itemVec1, long id2, Long2DoubleSortedMap itemVec2) {
        if (workMatrix == null) {
            throw new IllegalStateException("Model is already built");
        }

        // to profit from matrix symmetry, always store by the lesser id
        if (id1 < id2) {
            int coratings = 0;
            double deviation = 0.0;
            LongIterator iter = itemVec1.keySet().iterator();
            while (iter.hasNext()) {
                long u = iter.nextLong();
                if (itemVec2.containsKey(u)) {
                    coratings++;
                    deviation += itemVec1.get(u) - itemVec2.get(u);
                }
            }
            deviation = (coratings == 0) ? Double.NaN : deviation;

            Pair<Long2DoubleMap, Long2IntMap> row = workMatrix.get(id1);
            row.getLeft().put(id2, deviation);
            row.getRight().put(id2, coratings);
        }
    }

    /**
     * @return A matrix of item deviation and corating values to be used by
     *         a {@code SlopeOneItemScorer}.
     */
    public KeyedObjectMap<SlopeOneModel.ModelRow> buildMatrix() {
        if (workMatrix == null) {
            throw new IllegalStateException("Model is already built");
        }

        KeyedObjectMapBuilder<SlopeOneModel.ModelRow> builder = KeyedObjectMap.newBuilder();

        for (Map.Entry<Long, Pair<Long2DoubleMap, Long2IntMap>> e : workMatrix.entrySet()) {
            Long2DoubleMap vec = e.getValue().getLeft();
            Long2IntMap cor = e.getValue().getRight();
            SortedKeyIndex idx = SortedKeyIndex.fromCollection(vec.keySet());
            int n = idx.size();
            double[] deviations = new double[n];
            int[] counts = new int[n];

            for (int i = 0; i < n; i++) {
                long item = idx.getKey(i);
                double deviation = vec.get(item);
                int coratings = cor.get(item);
                deviations[i] = deviation / (coratings + damping);
                counts[i] = coratings;
            }

            builder.add(new SlopeOneModel.ModelRow(e.getKey(), idx, deviations, counts));
        }

        workMatrix = null;
        return builder.build();
    }
}
