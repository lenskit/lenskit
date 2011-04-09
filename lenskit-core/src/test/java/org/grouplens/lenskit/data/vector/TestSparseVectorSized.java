package org.grouplens.lenskit.data.vector;

/**
 * Re-run sparse vector tests with a longer version of the vector.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestSparseVectorSized extends TestSparseVector {
    /**
     * Construct a simple rating vector with three ratings.
     * @return A rating vector mapping {3, 7, 8} to {1.5, 3.5, 2}.
     */
    protected MutableSparseVector simpleVector() {
        long[] keys = {3, 7, 8, 2};
        double[] values = {1.5, 3.5, 2, 5};
        return new MutableSparseVector(keys, values, 3);
    }
}
