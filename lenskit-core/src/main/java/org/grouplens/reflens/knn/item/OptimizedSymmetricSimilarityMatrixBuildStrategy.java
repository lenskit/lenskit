/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.reflens.knn.item;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.LongIterator;

import org.grouplens.reflens.data.vector.SparseVector;
import org.grouplens.reflens.knn.OptimizableVectorSimilarity;
import org.grouplens.reflens.knn.SimilarityMatrix;
import org.grouplens.reflens.knn.SimilarityMatrixBuilder;
import org.grouplens.reflens.knn.SimilarityMatrixBuilderFactory;
import org.grouplens.reflens.knn.item.ItemItemRecommenderBuilder.BuildState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class OptimizedSymmetricSimilarityMatrixBuildStrategy implements
		SimilarityMatrixBuildStrategy {
	private static final Logger logger = LoggerFactory.getLogger(OptimizedSymmetricSimilarityMatrixBuildStrategy.class);
	private final SimilarityMatrixBuilderFactory matrixFactory;
	private final OptimizableVectorSimilarity<SparseVector> similarityFunction;

	OptimizedSymmetricSimilarityMatrixBuildStrategy(SimilarityMatrixBuilderFactory mfact, OptimizableVectorSimilarity<SparseVector> sim) {
		matrixFactory = mfact;
		similarityFunction = sim;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.knn.SimilarityMatrixBuildStrategy#buildMatrix(org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState)
	 */
	@Override
	public SimilarityMatrix buildMatrix(BuildState state) {
		final SimilarityMatrixBuilder builder = matrixFactory.create(state.itemCount);
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
	 * @see org.grouplens.reflens.knn.SimilarityMatrixBuildStrategy#needsUserItemSets()
	 */
	@Override
	public boolean needsUserItemSets() {
		return true;
	}

}
