package org.grouplens.lenskit.knn.user;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Compute the similarity between two users.
 * @author Michael Ekstrand
 * @since 0.11
 */
@DefaultImplementation(UserVectorSimilarity.class)
public interface UserSimilarity {
    /**
     * Compute the similarity between two users.
     * @param u1 The first user ID.
     * @param v1 The first user vector.
     * @param u2 The second user ID.
     * @param v2 The second user vector.
     * @return The similarity between the two users, in the range [0,1].
     */
    double similarity(long u1, SparseVector v1, long u2, SparseVector v2);

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
