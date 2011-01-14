package org.grouplens.reflens.knn;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.LongIterator;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;
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
