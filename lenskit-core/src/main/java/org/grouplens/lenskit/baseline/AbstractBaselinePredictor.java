package org.grouplens.lenskit.baseline;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import java.util.Collection;

/**
 * Abstract implementation of BaselinePredictor.
 * @author Michael Ekstrand
 */
public abstract class AbstractBaselinePredictor implements BaselinePredictor {
    /**
     * Implement new-vector predict in terms of
     * {@link #predict(long, SparseVector, MutableSparseVector)}.
     */
    public MutableSparseVector predict(long user, SparseVector ratings, Collection<Long> items) {
        MutableSparseVector v = new MutableSparseVector(items);
        predict(user, ratings, v);
        return v;
    }

    public void predict(long user, SparseVector ratings, MutableSparseVector output) {
        predict(user, ratings, output, true);
    }
}
