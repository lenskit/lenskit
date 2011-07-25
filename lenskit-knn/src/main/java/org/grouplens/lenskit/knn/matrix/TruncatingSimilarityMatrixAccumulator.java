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
package org.grouplens.lenskit.knn.matrix;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.io.Serializable;

import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.knn.params.ModelSize;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
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
        public TruncatingSimilarityMatrixAccumulator create(LongCollection entities) {
            return new TruncatingSimilarityMatrixAccumulator(maxNeighbors, entities);
        }
    }

    /**
     * Matrix implementation.
     */
    private static class Matrix implements SimilarityMatrix, Serializable {
        private static final long serialVersionUID = 6721870011265541987L;
        private Long2ObjectMap<ScoredLongList> neighborhoods;
        public Matrix(Long2ObjectMap<ScoredLongList> data) {
            neighborhoods = data;
        }
        
        @Override
        public ScoredLongList getNeighbors(long item) {
            return neighborhoods.get(item);
        }
    }

    private Long2ObjectMap<ScoredItemAccumulator> rows;
    private final int maxNeighbors;

    public TruncatingSimilarityMatrixAccumulator(@ModelSize int size,
            LongCollection entities) {
        logger.debug("Using neighborhood size of {} for {} items",
                     size, entities.size());
        maxNeighbors = size;
        rows = new Long2ObjectOpenHashMap<ScoredItemAccumulator>(entities.size());
        LongIterator it = entities.iterator();
        while (it.hasNext()) {
            rows.put(it.nextLong(), new ScoredItemAccumulator(maxNeighbors));
        }
    }

    @Override
    public SimilarityMatrix build() {
        Long2ObjectMap<ScoredLongList> data = new Long2ObjectOpenHashMap<ScoredLongList>(rows.size());
        for (Entry<ScoredItemAccumulator> row: rows.long2ObjectEntrySet()) {
            data.put(row.getLongKey(), row.getValue().finish());
        }
        Matrix m = new Matrix(data);
        rows = null;
        return m;
    }

    @Override
    public void put(long i1, long i2, double sim) {
        // We only accept nonnegative similarities
        if (sim <= 0.0) return;
        
        // concurrent read-only array access permitted
        ScoredItemAccumulator q = rows.get(i1);
        // synchronize on this row to add item
        synchronized (q) {
            q.put(i2, sim);
        }
    }

    @Override
    public void putSymmetric(long i1, long i2, double sim) {
        if (sim > 0.0) {
            put(i1, i2, sim);
            put(i2, i1, sim);
        }
    }
}
