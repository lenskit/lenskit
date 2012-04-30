package org.grouplens.lenskit.knn.item;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute the similarity between two items.
 * @author Michael Ekstrand
 * @since 0.11
 */
@DefaultImplementation(ItemVectorSimilarity.class)
public interface ItemSimilarity {
    /**
     * Compute the similarity between two items.
     * @param i1 The first item ID.
     * @param v1 The first item vector.
     * @param i2 The second item ID.
     * @param v2 The second item vector.
     * @return The similarity between the two items, in the range [0,1].
     */
    double similarity(long i1, SparseVector v1, long i2, SparseVector v2);

    /**
     * Query whether this similarity is sparse.
     * @return {@code true} if the similarity function is sparse.
     * @see org.grouplens.lenskit.knn.VectorSimilarity#isSparse()
     */
    boolean isSparse();

    /**
     * Query whether this similarity is symmetric.
     * @return {@code true} if the similarity function is symmetric.
     * @see org.grouplens.lenskit.knn.VectorSimilarity#isSymmetric()
     */
    boolean isSymmetric();
}
