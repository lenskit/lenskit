package org.grouplens.lenskit.transform.truncate;

import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * A {@code VectorTruncator} that does not actually perform any truncation.
 * Any input vector is left unchanged.
 */
public class NoOpTruncator implements VectorTruncator {
    @Override
    public void truncate(MutableSparseVector v) {}
}
