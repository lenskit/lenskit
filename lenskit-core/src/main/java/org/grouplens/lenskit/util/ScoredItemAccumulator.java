package org.grouplens.lenskit.util;

import org.grouplens.lenskit.data.ScoredLongArrayList;
import org.grouplens.lenskit.data.ScoredLongList;

import it.unimi.dsi.fastutil.doubles.DoubleHeapIndirectPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollections;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Accumulate the top <i>N</i> scored IDs.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ScoredItemAccumulator {
    private static final long serialVersionUID = -3045709409904317792L;
    private final int count;
    private double[] scores;
    private long[] items;
    private int slot;
    private int size;
    private DoubleHeapIndirectPriorityQueue heap;

    public ScoredItemAccumulator(int n) {
        this.count = n;
        scores = new double[n+1];
        items = new long[n+1];
        slot = 0;
        size = 0;
        heap = new DoubleHeapIndirectPriorityQueue(scores);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void put(long i, double val) {
        assert slot <= count;
        assert heap.size() == size;
        /* Store the new item. The slot shows where the current item is,
         * and then we deal with it based on whether we're oversized.
         */
        items[slot] = i;
        scores[slot] = val;
        heap.enqueue(slot);

        if (size == count) {
            // already at capacity, so remove and reuse smallest item
            slot = heap.dequeue();
        } else {
            // we have free space, so increment the slot and size
            slot += 1;
            size += 1;
        }
    }
    
    /**
     * Accumulate the scores into a sorted scored list and reset the accumulator.
     * @return The sorted, scored list of items.
     */
    public ScoredLongList finish() {
        assert size == heap.size();
        int[] indices = new int[size];
        for (int i = size - 1; i >= 0; i--) {
            indices[i] = heap.dequeue();
        }
        ScoredLongList l = new ScoredLongArrayList(size);
        for (int i: indices) {
            l.add(items[i], scores[i]);
        }
        
        size = 0;
        slot = 0;
        return l;
    }
}
