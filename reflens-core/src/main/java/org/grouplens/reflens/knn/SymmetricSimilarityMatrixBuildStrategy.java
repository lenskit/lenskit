package org.grouplens.reflens.knn;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.knn.ItemItemRecommenderBuilder.BuildState;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SymmetricSimilarityMatrixBuildStrategy implements
		SimilarityMatrixBuildStrategy {
	private final static Logger logger = LoggerFactory.getLogger(SymmetricSimilarityMatrixBuildStrategy.class);
	
	private final SimilarityMatrixBuilderFactory matrixFactory;
	private final Similarity<? super RatingVector> similarityFunction;
	
	SymmetricSimilarityMatrixBuildStrategy(SimilarityMatrixBuilderFactory matrixFactory, Similarity<? super RatingVector> similarity) {
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
			for (int j = i+1; j < nitems; j++) {
				double sim = similarityFunction.similarity(state.itemRatings.get(i), state.itemRatings.get(j));
				builder.putSymmetric(i, j, sim);
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
