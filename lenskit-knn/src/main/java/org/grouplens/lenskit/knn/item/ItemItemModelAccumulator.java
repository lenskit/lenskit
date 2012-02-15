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

import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accumulator for item similarities that go into the item-item CF model.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class ItemItemModelAccumulator {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelAccumulator.class);

    private Long2ObjectMap<ScoredItemAccumulator> columns;
    private final LongSortedSet itemUniverse;

    public ItemItemModelAccumulator(@ModelSize int size,
            LongSortedSet entities) {
        logger.debug("Using model size of {} for {} items",
                     size, entities.size());
        itemUniverse = entities;
        columns = new Long2ObjectOpenHashMap<ScoredItemAccumulator>(entities.size());
        LongIterator it = entities.iterator();
        while (it.hasNext()) {
            columns.put(it.nextLong(), new TopNScoredItemAccumulator(size));
        }
    }

    public ItemItemModel build() {
        Long2ObjectMap<ScoredLongList> data = new Long2ObjectOpenHashMap<ScoredLongList>(columns.size());
        for (Entry<ScoredItemAccumulator> colEntry: columns.long2ObjectEntrySet()) {
            final long j = colEntry.getLongKey();
            ScoredLongList column = colEntry.getValue().finish();
            colEntry.setValue(null);
            ScoredLongListIterator iter = column.iterator();
            while (iter.hasNext()) {
                final long i = iter.nextLong();
                final double s = iter.getScore();
                ScoredLongList row = data.get(i);
                if (row == null) {
                    row = new ScoredLongArrayList();
                    data.put(i, row);
                }
                row.add(j, s);
            }
        }
        for (ScoredLongList row: data.values()) {
            ScoredLongArrayList impl = (ScoredLongArrayList) row;
            impl.trim();
            impl.sort(DoubleComparators.OPPOSITE_COMPARATOR);
        }
        ItemItemModel model = new ItemItemModel(itemUniverse, data);
        columns = null;
        return model;
    }

    /**
     * Store an entry in the similarity matrix.
     * @param i The matrix row (an item ID).
     * @param j The matrix column (an item ID).
     * @param sim The similarity between items {@code j} and {@code i}. As documented in the
     *            {@link org.grouplens.lenskit.knn.item package docs}, this is \(s(j,i)\).
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void put(long i, long j, double sim) {
        // We only accept nonnegative similarities
        if (sim <= 0.0) return;

        // concurrent read-only array access permitted
        ScoredItemAccumulator q = columns.get(j);
        // synchronize on this row to add item
        synchronized (q) {
            q.put(i, sim);
        }
    }
}
