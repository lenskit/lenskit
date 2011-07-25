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
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulator;
import org.grouplens.lenskit.knn.matrix.SimilarityMatrixAccumulatorFactory;

/**
 * Build strategy that harnesses the symmetric nature of some similarity
 * functions. It simply overrides {@link #innerIterator(LongSortedSet, long)} to
 * return an iterator only considering items after the outer item.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
class SymmetricModelBuildStrategy extends SimpleModelBuildStrategy {
    SymmetricModelBuildStrategy(SimilarityMatrixAccumulatorFactory matrixFactory, Similarity<? super SparseVector> similarity) {
        super(matrixFactory, similarity);
    }

    @Override
    protected LongIterator innerIterator(LongSortedSet items, long outer) {
        return items.iterator(outer);
    }
    
    @Override
    protected void put(SimilarityMatrixAccumulator ma, long i, long j, double sim) {
        ma.putSymmetric(i, j, sim);
    }
}
