package org.grouplens.lenskit.norm;

import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractUserRatingVectorNormalizer implements
        UserRatingVectorNormalizer {

    /**
     * Implementation that delegates to {@link #makeTransformation(long, SparseVector)}
     * and the resulting {@link VectorTransformation}.
     */
    @Override
    public void normalize(long userId, SparseVector ratings,
            MutableSparseVector vector) {
        VectorTransformation tform = makeTransformation(userId, ratings);
        tform.apply(vector);
    }

    /**
     * Implementation that delegates to {@link #normalize(long, SparseVector, MutableSparseVector)}.
     */
    @Override
    public void normalize(long userId, MutableSparseVector ratings) {
        normalize(userId, ratings, ratings);
    }
}
