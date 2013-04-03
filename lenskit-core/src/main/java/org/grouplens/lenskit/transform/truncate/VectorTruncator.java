package org.grouplens.lenskit.transform.truncate;


import org.grouplens.lenskit.vectors.MutableSparseVector;

/**
 * Truncates a vector
 */
public interface VectorTruncator {

    /**
     * Truncate a vector, removing any entries that do not satisfy some condition.
     * The vector is modified in place, i.e. the vector argument will be modified
     * directly.
     * @param v The vector to truncate.
     */
    public void truncate(MutableSparseVector v);

}
