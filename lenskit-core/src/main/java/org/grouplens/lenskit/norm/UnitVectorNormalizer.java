package org.grouplens.lenskit.norm;

import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Vector normalizer that scales a vector by the factor needed to scale the
 * reference vector to a unit vector. If the length of the reference vector
 * is 0, no normalization is applied.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UnitVectorNormalizer extends AbstractVectorNormalizer<ImmutableSparseVector> {
    private final double tolerance;
    
    /**
     * Create a unit vector normalizer with a tolerance of 1.0e-6.
     */
    public UnitVectorNormalizer() {
        this(1.0e-6);
    }
    
    /**
     * Create a unit vector normalizer a specified tolerance around 0. Any
     * vector norm whose absolute value is less than <var>t</var> is converted
     * to a no-op.
     * @param t The error tolerance for 0-checking.
     */
    public UnitVectorNormalizer(double t) {
        tolerance = t;
    }

    @Override
    public VectorTransformation makeTransformation(ImmutableSparseVector reference) {
        double s = reference.norm();
        if (Math.abs(s) < tolerance) {
            return new IdentityVectorNormalizer().makeTransformation(reference);
        } else {
            return new ScalingTransform(s);
        }
    }
    
    static class ScalingTransform implements VectorTransformation {
        final double factor;
        
        public ScalingTransform(double f) {
            factor = f;
        }

        @Override
        public MutableSparseVector apply(MutableSparseVector vector) {
            vector.scale(1/factor);
            return vector;
        }

        @Override
        public MutableSparseVector unapply(MutableSparseVector vector) {
            vector.scale(factor);
            return vector;
        }
        
    }
}