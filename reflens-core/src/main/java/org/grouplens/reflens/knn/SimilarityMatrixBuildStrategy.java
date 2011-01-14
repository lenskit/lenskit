package org.grouplens.reflens.knn;

import org.grouplens.reflens.util.SimilarityMatrix;

/**
 * A strategy for computing similarity matrices.
 * 
 * {@link ItemItemRecommenderBuilder} uses the Strategy pattern to optimize its
 * build algorithm based on what kind of similarity function is in use.  This is
 * the interface which makes that possible.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
interface SimilarityMatrixBuildStrategy {
	/**
	 * Query whether this strategy requires the build state to have easy access
	 * to the sets of items rated by each user.
	 * @return {@code true} if the strategy requires the item sets.
	 */
	boolean needsUserItemSets();
	
	/**
	 * Build the item-item matrix.
	 * @param state The build state containing data needed to build the matrix.
	 * @return The completed similarity matrix
	 */
	SimilarityMatrix buildMatrix(ItemItemRecommenderBuilder.BuildState state);
}
