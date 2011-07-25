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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.OptimizableVectorSimilarity;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrix;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulator;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulatorFactory;

/**
 * Model build strategy that avoids computing similarities between items with
 * disjoint rating sets.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SparseModelBuildStrategy implements
        ItemItemModelBuildStrategy {
    private final SimilarityMatrixAccumulatorFactory matrixFactory;
    private final OptimizableVectorSimilarity<SparseVector> similarityFunction;

    SparseModelBuildStrategy(SimilarityMatrixAccumulatorFactory mfact, OptimizableVectorSimilarity<SparseVector> sim) {
        matrixFactory = mfact;
        similarityFunction = sim;
    }

    @Override
    public SimilarityMatrix buildMatrix(ItemItemModelBuilder state) {
        final LongSortedSet items = state.getItems();
        final SimilarityMatrixAccumulator builder = matrixFactory.create(items);
        
        LongIterator iit = items.iterator();
        while (iit.hasNext()) {
            final long i = iit.nextLong();
            final SparseVector v = state.itemVector(i);
            final LongSet candidates = new LongOpenHashSet();
            final LongIterator uiter = v.keySet().iterator();
            while (uiter.hasNext()) {
                final long user = uiter.next();
                LongSortedSet uitems = state.userItemSet(user);
                LongSet uss = subset(uitems, i);
                candidates.addAll(uss);
            }

            final LongIterator iter = candidates.iterator();
            while (iter.hasNext()) {
                final long j = iter.nextLong();
                if (i == j) continue;
                
                final double sim = 
                        similarityFunction.similarity(v, state.itemVector(j));
                put(builder, i, j, sim);
            }
        }
        return builder.build();
    }
    
    /**
     * Subset a user's items for use with the outer item.
     * 
     * @param userItems The items for the current user.
     * @param outerItem The item in the outer loop.
     * @return The user's items to use as candidate neighbors for
     *         <var>outerItem</var>. This list is allowed to contain
     *         <var>outerItem</var>, as it is transparently skipped.
     */
    protected LongSet subset(LongSortedSet userItems, long outerItem) {
        return userItems;
    }
    
    /**
     * Store a similarity.
     * @param builder The accumulator in which to store the similarity.
     * @param i The first item.
     * @param j The second item.
     * @param sim
     */
    protected void put(SimilarityMatrixAccumulator builder, long i, long j, double sim) {
        builder.put(i,  j, sim);
    }

    @Override
    public boolean needsUserItemSets() {
        return true;
    }

}
