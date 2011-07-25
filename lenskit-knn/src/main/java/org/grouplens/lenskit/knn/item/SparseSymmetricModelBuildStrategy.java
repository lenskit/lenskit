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

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import org.grouplens.lenskit.data.vector.SparseVector;
import org.grouplens.lenskit.knn.OptimizableVectorSimilarity;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulator;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulatorFactory;

/**
 * Build strategy that uses symmetric similarities and avoids computing similarity
 * between items with disjoint rating sets.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SparseSymmetricModelBuildStrategy extends SparseModelBuildStrategy {
    SparseSymmetricModelBuildStrategy(SimilarityMatrixAccumulatorFactory mfact, OptimizableVectorSimilarity<SparseVector> sim) {
        super(mfact, sim);
    }

    @Override
    protected LongSet subset(LongSortedSet userItems, long outerItem) {
        return userItems.headSet(outerItem);
    }
    
    @Override
    protected void put(SimilarityMatrixAccumulator ma, long i, long j, double sim) {
        ma.putSymmetric(i, j, sim);
    }
}
