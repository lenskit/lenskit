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

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.LongIterator;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.OptimizableVectorSimilarity;
import org.grouplens.lenskit.knn.SimilarityMatrix;
import org.grouplens.lenskit.knn.SimilarityMatrixAccumulator;
import org.grouplens.lenskit.knn.SimilarityMatrixAccumulatorFactory;
import org.grouplens.lenskit.knn.item.ItemItemRecommenderEngineBuilder.BuildState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build strategy that uses symmetric similarities and avoids computing similarity
 * between items with disjoint rating sets.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SparseSymmetricModelBuildStrategy implements
        ItemItemModelBuildStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SparseSymmetricModelBuildStrategy.class);
    private final SimilarityMatrixAccumulatorFactory matrixFactory;
    private final OptimizableVectorSimilarity<SparseVector> similarityFunction;

    SparseSymmetricModelBuildStrategy(SimilarityMatrixAccumulatorFactory mfact, OptimizableVectorSimilarity<SparseVector> sim) {
        matrixFactory = mfact;
        similarityFunction = sim;
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.knn.SimilarityMatrixBuildStrategy#buildMatrix(org.grouplens.lenskit.knn.ItemItemRecommenderBuilder.BuildState)
     */
    @Override
    public SimilarityMatrix buildMatrix(BuildState state) {
        final SimilarityMatrixAccumulator builder = matrixFactory.create(state.itemCount);
        final int nitems = state.itemCount;
        logger.debug("Building matrix with {} rows", nitems);
        for (int i = 0; i < nitems; i++) {
            final SparseVector v = state.itemRatings.get(i);
            final IntSet candidates = new IntOpenHashSet();
            final LongIterator uiter = v.keySet().iterator();
            while (uiter.hasNext()) {
                long user = uiter.next();
                IntSortedSet uitems = state.userItemSets.get(user).headSet(i);
                candidates.addAll(uitems);
            }

            final IntIterator iter = candidates.iterator();
            while (iter.hasNext()) {
                final int j = iter.nextInt();
                final double sim = similarityFunction.similarity(v,
                        state.itemRatings.get(j));
                builder.putSymmetric(i,j,sim);
            }
        }
        return builder.build();
    }

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.knn.SimilarityMatrixBuildStrategy#needsUserItemSets()
     */
    @Override
    public boolean needsUserItemSets() {
        return true;
    }

}
