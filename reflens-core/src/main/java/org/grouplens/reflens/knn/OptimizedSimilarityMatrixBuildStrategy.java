/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.knn;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.LongIterator;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class OptimizedSimilarityMatrixBuildStrategy implements
		SimilarityMatrixBuildStrategy {
	private static final Logger logger = LoggerFactory.getLogger(OptimizedSimilarityMatrixBuildStrategy.class);
	private final SimilarityMatrixBuilderFactory matrixFactory;
	private final OptimizableVectorSimilarity<RatingVector> similarityFunction;

	OptimizedSimilarityMatrixBuildStrategy(SimilarityMatrixBuilderFactory mfact, OptimizableVectorSimilarity<RatingVector> sim) {
		matrixFactory = mfact;
		similarityFunction = sim;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.knn.SimilarityMatrixBuildStrategy#buildMatrix(org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState)
	 */
	@Override
	public SimilarityMatrix buildMatrix(BuildState state) {
		final SimilarityMatrixBuilder builder = matrixFactory.create(state.itemCount);
		logger.debug("Building with AVL tree implementation");
		final int nitems = state.itemCount;
		for (int i = 0; i < nitems; i++) {
			final RatingVector v = state.itemRatings.get(i);
			final IntSet candidates = new IntOpenHashSet();
			final LongIterator uiter = v.idSet().iterator();
			while (uiter.hasNext()) {
				long user = uiter.next();
				IntSortedSet uitems = state.userItemSets.get(user);
				candidates.addAll(uitems);
			}
			candidates.rem(i);
			
			final IntIterator iter = candidates.iterator();
			while (iter.hasNext()) {
				final int j = iter.nextInt();
				final double sim = similarityFunction.similarity(v,
						state.itemRatings.get(j));
				builder.put(i,j,sim);
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
