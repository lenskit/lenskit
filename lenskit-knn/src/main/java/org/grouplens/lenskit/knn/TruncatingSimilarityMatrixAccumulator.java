/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.knn;

import it.unimi.dsi.fastutil.doubles.DoubleHeapIndirectPriorityQueue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.util.IndexedItemScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TruncatingSimilarityMatrixAccumulator creates SimilarityMatrices where rows
 * are truncated to a specific size, so only the top N similar items are stored
 * in each row. The created matrices are Serializable.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class TruncatingSimilarityMatrixAccumulator implements SimilarityMatrixAccumulator {
    private static final Logger logger = LoggerFactory.getLogger(TruncatingSimilarityMatrixAccumulator.class);

    /**
     * The SimilarityMatrixAccumulatorFactory to use when creating
     * TruncatingSimilarityMatrixAccumulators.
     * 
     * @author Michael Ludwig <mludwig@cs.umn.edu>
     */
    public static class Factory implements SimilarityMatrixAccumulatorFactory {
        private final int maxNeighbors;
        
        public Factory(@ModelSize int maxNeighbors) {
            this.maxNeighbors = maxNeighbors;
        }

        @Override
        public TruncatingSimilarityMatrixAccumulator create(int nrows) {
            return new TruncatingSimilarityMatrixAccumulator(maxNeighbors, nrows);
        }
    }
    
    static final class Score implements IndexedItemScore {
        private final int index;
        private final double score;

        public Score(final int i, final double s) {
            index = i;
            score = s;
        }

        public int getIndex() {
            return index;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * Priority queue for tracking the <i>N</i> highest-scoring items.
     *
     * This uses a {@link DoubleHeapIndirectPriorityQueue} to maintain a heap of
     * items in parallel unboxed arrays of scores and indices.  The arrays are
     * of length <var>maxNeighbors</var>+1; this allows them to have one free
     * slot to hold a new item when the queue is already full.
     *
     * Iteration order is undefined for this class.
     *
     * @author Michael Ekstrand <ekstrand@cs.umn.edu>
     *
     */
    static final class ScoreQueue implements Iterable<IndexedItemScore>, Serializable {
        private static final long serialVersionUID = -3045709409904317792L;
        private final int maxNeighbors;
        private double[] scores;
        private int[] indices;
        private int slot;
        private int size;
        private DoubleHeapIndirectPriorityQueue heap;

        public ScoreQueue(int nbrs) {
            this.maxNeighbors = nbrs;
            scores = new double[nbrs+1];
            indices = new int[nbrs+1];
            slot = 0;
            size = 0;
            heap = new DoubleHeapIndirectPriorityQueue(scores);
        }

        @Override
        public Iterator<IndexedItemScore> iterator() {
            return new Iterator<IndexedItemScore>() {
                private int pos = 0;

                @Override
                public boolean hasNext() {
                    return pos < size;
                }

                // TODO Support fast iteration
                @Override
                public IndexedItemScore next() {
                    if (pos < size) {
                        int i = pos;
                        if (i >= slot) i++;   // skip the slot
                        Score s = new Score(indices[i], scores[i]);
                        pos += 1;
                        return s;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }

        public void put(int i, double sim) {
            if (heap == null)
                throw new RuntimeException("attempt to add to finished queue");
            assert slot <= maxNeighbors;
            assert heap.size() == size;
            /* Store the new item. The slit shows where the current item is,
             * and then we deal with it based on whether we're oversized.
             */
            indices[slot] = i;
            scores[slot] = sim;
            heap.enqueue(slot);

            if (size == maxNeighbors) {
                // already at capacity, so remove and reuse smallest item
                slot = heap.dequeue();
            } else {
                // we have free space, so increment the slot and size
                slot += 1;
                size += 1;
            }
        }

        /**
         * Free internal structures needed for adding items.  After calling this
         * method, it is an error to call {@link #put(int, double)}.
         */
        public void finish() {
            heap = null;
        }
    }

    private static class Matrix implements SimilarityMatrix, Serializable {
        private static final long serialVersionUID = 6721870011265541987L;
        private ScoreQueue[] rows;
        public Matrix(ScoreQueue[] rows) {
            this.rows = rows;
        }
        @Override
        public Iterable<IndexedItemScore> getNeighbors(int i) {
            return rows[i];
        }
        @Override
        public int size() {
            return rows.length;
        }
    }

    private ScoreQueue[] rows;
    private final int maxNeighbors;
    private final int itemCount;

    public TruncatingSimilarityMatrixAccumulator(int neighborhoodSize, int nitems) {
        logger.debug("Using neighborhood size of {} for {} items", neighborhoodSize, nitems);
        maxNeighbors = neighborhoodSize;
        this.itemCount = nitems;
        setup();
    }

    private void setup() {
        rows = new ScoreQueue[itemCount];
        for (int i = 0; i < itemCount; i++) {
            rows[i] = new ScoreQueue(maxNeighbors);
        }
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.util.SimilarityMatrix#finish()
     */
    @Override
    public SimilarityMatrix build() {
        for (ScoreQueue row: rows) {
            row.finish();
        }
        Matrix m = new Matrix(rows);
        rows = null;
        return m;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.util.SimilarityMatrix#put(int, int, double)
     */
    @Override
    public void put(int i1, int i2, double sim) {
        if (sim < 0.0) return;
        if (i2 < 0 || i2 >= rows.length)
            throw new IndexOutOfBoundsException();
        // concurrent read-only array access permitted
        ScoreQueue q = rows[i1];
        // synchronize on this row to add item
        synchronized (q) {
            q.put(i2, sim);
        }
    }

    @Override
    public void putSymmetric(int i1, int i2, double sim) {
        if (sim > 0.0) {
            put(i1, i2, sim);
            put(i2, i1, sim);
        }
    }

    @Override
    public int size() {
        return itemCount;
    }
}
