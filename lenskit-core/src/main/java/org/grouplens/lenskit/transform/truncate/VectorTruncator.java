package org.grouplens.lenskit.transform.truncate;


import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Used to perform truncation on a vector.
 */
@DefaultImplementation(NoOpTruncator.class)
public interface VectorTruncator {

    /**
     * Truncate a vector, removing any entries that do not satisfy some condition.
     * The vector is modified in place, i.e. the vector argument will be modified
     * directly.
     * @param v The vector to truncate.
     */
    public void truncate(MutableSparseVector v);

}
