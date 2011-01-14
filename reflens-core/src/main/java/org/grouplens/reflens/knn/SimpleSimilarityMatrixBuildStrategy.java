package org.grouplens.reflens.knn;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity matrix strategy that assumes nothing about the similarity function.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class SimpleSimilarityMatrixBuildStrategy implements
		SimilarityMatrixBuildStrategy {
	private final static Logger logger = LoggerFactory.getLogger(SimpleSimilarityMatrixBuildStrategy.class);
	
	private final SimilarityMatrixBuilderFactory matrixFactory;
	private final Similarity<? super RatingVector> similarityFunction;
	
	SimpleSimilarityMatrixBuildStrategy(SimilarityMatrixBuilderFactory matrixFactory, Similarity<? super RatingVector> similarity) {
		this.matrixFactory = matrixFactory;
		this.similarityFunction = similarity;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.knn.SimilarityMatrixBuildStrategy#buildMatrix(org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState)
	 */
	@Override
	public SimilarityMatrix buildMatrix(BuildState state) {
		final int nitems = state.itemCount;
		logger.debug("Building matrix with {} rows");
		SimilarityMatrixBuilder builder = matrixFactory.create(state.itemCount);
		for (int i = 0; i < nitems; i++) {
			for (int j = 0; j < nitems; j++) {
				if (i == j) continue;
				double sim = similarityFunction.similarity(state.itemRatings.get(i), state.itemRatings.get(j));
				builder.put(i, j, sim);
			}
		}
		return builder.build();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.knn.SimilarityMatrixBuildStrategy#needsUserItemSets()
	 */
	@Override
	public boolean needsUserItemSets() {
		return false;
	}

}
