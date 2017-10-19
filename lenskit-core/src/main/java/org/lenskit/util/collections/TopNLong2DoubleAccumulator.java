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
package org.lenskit.util.collections;

import com.google.common.primitives.Doubles;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

/**
 * Accumulate the top <i>N</i> scored IDs.  IDs are sorted by their associated
 * scores.
 */
public final class TopNLong2DoubleAccumulator implements Long2DoubleAccumulator {
    private final int targetCount;
    private DoubleArrayList scores;
    private CompactableLongArrayList items;

    // The index of the empty space to use.  Once the accumulator is at capacity, this will be the
    // index of the last-removed item.
    private int slot;
    // The current size of the accumulator.
    private int size;
    private IntPriorityQueue heap;

    /**
     * Create a new accumulator to accumulate the top <var>n</var> IDs.
     *
     * @param n The number of IDs to retain.
     */
    public TopNLong2DoubleAccumulator(int n) {
        this.targetCount = n;

        slot = 0;
        size = 0;

        int isz = findInitialSize(targetCount + 1);
        scores = new DoubleArrayList(isz);
        items = new CompactableLongArrayList(isz);
        heap = new IntHeapPriorityQueue(this::comparePositions);

        // item lists are lazy-allocated
    }

    private int comparePositions(int i1, int i2) {
        return Doubles.compare(scores.getDouble(i1), scores.getDouble(i2));
    }

    /**
     * Find a good initial size to minimize the overhead when up to <em>n</em> items are added to a
     * list.
     *
     * @param maxSize The maximum number of items expected.
     * @return A size in the range [10,25] that, when used as the initial size of an array
     *         list, minimizes the overhead when {@code maxSize} items have been added.
     */
    private static int findInitialSize(int maxSize) {
        int best = 10;
        int overhead = maxSize;
        for (int i = 10; i <= 25; i++) {
            int cap = i;
            while (cap < maxSize) {
                cap *= 2;
            }
            int ovh = maxSize - cap;
            if (ovh < overhead) {
                overhead = ovh;
                best = i;
            }
        }
        return best;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(long item, double score) {
        assert slot <= targetCount;
        assert heap.size() == size;

        if (items == null) {
            int isz = findInitialSize(targetCount + 1);
            scores = new DoubleArrayList(isz);
            items = new CompactableLongArrayList(isz);
            heap = new IntHeapPriorityQueue(this::comparePositions);
        }

        /*
         * Store the new item. The slot shows where the current item is, and
         * then we deal with it based on whether we're oversized.
         */
        if (slot == items.size()) {
            // we are still adding items
            items.add(item);
            scores.add(score);
        } else {
            // we are reusing slots
            if (!heap.isEmpty() && score <= scores.getDouble(heap.firstInt())) {
                return; // the item won't beat anything else
            }
            items.set(slot, item);
            scores.set(slot, score);
        }
        heap.enqueue(slot);

        if (size == targetCount) {
            // already at capacity, so remove and reuse smallest item
            slot = heap.dequeueInt();
        } else {
            // we have free space, so increment the slot and size
            slot += 1;
            size += 1;
        }
    }

    @Override
    public Long2DoubleMap finishMap() {
        if (scores == null) {
            return Long2DoubleMaps.EMPTY_MAP;
        }

        assert size == heap.size();
        int[] indices = new int[size];
        // Copy backwards so the scored list is sorted.
        for (int i = size - 1; i >= 0; i--) {
            indices[i] = heap.dequeueInt();
        }
        assert heap.isEmpty();

        long[] keys = new long[indices.length];
        double[] values = new double[indices.length];
        for (int i = 0; i < indices.length; i++) {
            keys[i] = items.getLong(indices[i]);
            values[i] = scores.getDouble(indices[i]);
        }
        clear();

        return Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);
    }

    @Override
    public LongSet finishSet() {
        assert size == heap.size();

        LongSet longs = new LongOpenHashSet(size);
        while (!heap.isEmpty()) {
            longs.add(items.getLong(heap.dequeueInt()));
        }
        clear();

        return longs;
    }

    @Override
    public LongList finishList() {
        assert size == heap.size();
        int[] indices = new int[size];
        // Copy backwards so the scored list is sorted.
        for (int i = size - 1; i >= 0; i--) {
            indices[i] = heap.dequeueInt();
        }
        LongList list = new LongArrayList(size);
        for (int i : indices) {
            list.add(items.getLong(i));
        }

        assert heap.isEmpty();

        clear();

        return list;
    }

    private void clear() {
        size = 0;
        slot = 0;
        items = null;
        scores = null;
    }
}
