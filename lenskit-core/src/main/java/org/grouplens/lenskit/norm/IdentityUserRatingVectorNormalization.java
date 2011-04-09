package org.grouplens.lenskit.norm;

import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * Identity normalization (makes no change).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IdentityUserRatingVectorNormalization extends
        AbstractUserRatingVectorNormalizer {
    private static final VectorTransformation IDENTITY_TRANSFORM = new VectorTransformation() {
        
        @Override
        public void unapply(MutableSparseVector vector) {
            return;
        }
        
        @Override
        public void apply(MutableSparseVector vector) {
            return;
        }
    };

    /* (non-Javadoc)
     * @see org.grouplens.lenskit.norm.UserRatingVectorNormalizer#makeTransformation(long, org.grouplens.lenskit.data.vector.SparseVector)
     */
    @Override
    public VectorTransformation makeTransformation(long userId,
            SparseVector ratings) {
        return IDENTITY_TRANSFORM;
    }

}
