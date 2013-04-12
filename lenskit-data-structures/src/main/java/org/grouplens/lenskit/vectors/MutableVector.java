package org.grouplens.lenskit.vectors;

import com.google.common.base.Preconditions;

/**
 * Mutable {@link org.grouplens.lenskit.vectors.Vector}.  This vector can be modified and is not
 * thread-safe.
 *
 * @compat Experimental — this interface may change in future versions of LensKit.
 */
public class MutableVector extends Vector {
    MutableVector(double[] v) {
        super(v);
    }

    /**
     * Create a new vector wrapping an existing array.
     *
     * @param data The data array.
     * @return A vector backed by {@code data}.
     */
    public static MutableVector wrap(double[] data) {
        return new MutableVector(data);
    }

    /**
     * Set a value in this vector.
     * @param i The index.
     * @param v The value to set.
     * @return The old value at {@code i}.
     * @throws IllegalArgumentException if {@code i} is not a valid index in this vector.
     */
    public double set(int i, double v) {
        Preconditions.checkElementIndex(i, dim());
        markModified();
        final double old = data[i];
        data[i] = v;
        return old;
    }

    /**
     * Add another vector to this vector.
     * @param v The other vector.
     * @throws IllegalArgumentException if {@code v} has a different dimension than this vector.
     */
    public void add(Vector v) {
        Preconditions.checkArgument(v.dim() == dim(), "incompatible vector dimensions");
        markModified();
        final int sz = dim();
        for (int i = 0; i < sz; i++) {
            data[i] += v.data[i];
        }
    }
}
