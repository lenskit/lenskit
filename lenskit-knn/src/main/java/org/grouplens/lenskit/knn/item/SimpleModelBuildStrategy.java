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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.Similarity;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrix;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulator;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity matrix strategy that assumes nothing about the similarity function.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SimpleModelBuildStrategy implements
        ItemItemModelBuildStrategy {
    private final static Logger logger = LoggerFactory.getLogger(SimpleModelBuildStrategy.class);

    private final SimilarityMatrixAccumulatorFactory matrixFactory;
    private final Similarity<? super SparseVector> similarityFunction;

    SimpleModelBuildStrategy(SimilarityMatrixAccumulatorFactory matrixFactory, Similarity<? super SparseVector> similarity) {
        this.matrixFactory = matrixFactory;
        this.similarityFunction = similarity;
    }

    @Override
    public SimilarityMatrix buildMatrix(ItemItemModelBuilder state) {
        final int nitems = state.getItemCount();
        logger.debug("Building matrix with {} rows", nitems);
        LongSortedSet items = state.getItems();
        SimilarityMatrixAccumulator builder = matrixFactory.create(items);
        LongIterator iit = items.iterator();
        while (iit.hasNext()) {
            final long i = iit.nextLong();
            LongIterator jit = innerIterator(items, i);
            while (jit.hasNext()) {
                final long j = jit.nextLong();
                if (i == j) continue;
                double sim = similarityFunction.similarity(state.itemVector(i), state.itemVector(j));
                put(builder, i, j, sim);
            }
        }
        return builder.build();
    }
    
    /**
     * Get the iterator for the inner loop over the items.
     * @param items The item collection.
     * @param outer The item we're at in the outer loop.
     * @return The iterator to use for the inner loop.
     */
    protected LongIterator innerIterator(LongSortedSet items, long outer) {
        return items.iterator();
    }

    @Override
    public boolean needsUserItemSets() {
        return false;
    }

    /**
     * @param ma
     * @param i
     * @param j
     * @param sim
     */
    protected void put(SimilarityMatrixAccumulator ma, long i, long j,
                       double sim) {
        ma.put(i, j, sim);
    }

}
