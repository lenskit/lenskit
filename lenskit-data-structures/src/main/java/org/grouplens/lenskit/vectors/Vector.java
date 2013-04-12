package org.grouplens.lenskit.vectors;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Arrays;

import static java.lang.Math.sqrt;

/**
 * A real vector.  This stores an immutable vector of doubles, starting with 0.
 */
@Immutable
public class Vector implements Serializable {
    private static final long serialVersionUID = 1L;

    private final double[] data;

    private transient volatile Double norm;
    private transient volatile Double sum;
    private transient volatile int hashCode;

    private Vector(double[] d) {
        data = d;
    }

    /**
     * Create a new vector wrapping an existing array.  The array <b>must not</b> be modified
     * after being wrapped.
     *
     * @param data The data array.
     * @return A vector backed by {@code data}.
     */
    public static Vector wrap(double[] data) {
        return new Vector(data);
    }

    /**
     * Get the value from the vector at the specified position.
     * @param i The index into the vector.
     * @return The value at index {@code i}.
     * @throws IllegalArgumentException if {@code i} is not in the range [0,{@link #dim()}).
     */
    public double get(int i) {
        Preconditions.checkElementIndex(i, data.length);
        return data[i];
    }

    /**
     * Get the dimension of this vector (the number of elements).
     * @return The number of elements in the vector.
     */
    public int dim() {
        return data.length;
    }

    /**
     * Get the L2 (Euclidean) norm of this vector.
     * @return The Euclidean length of the vector.
     */
    public double norm() {
        if (norm == null) {
            double ssq = 0;
            final int sz = data.length;
            for (double v : data) {
                ssq += v * v;
            }
            norm = sqrt(ssq);
        }
        return norm;
    }

    /**
     * Get the sum of this vector.
     * @return The sum of the elements of the vector.
     */
    public double sum() {
        if (sum == null) {
            double s = 0;
            for (double v : data) {
                s += v;
            }
            sum = s;
        }
        return sum;
    }

    /**
     * Compute the dot product of this vector with another.
     * @param other The other vector.
     * @return The dot product of this vector and {@code other}.
     * @throws IllegalArgumentException if {@code other.dim() != this.dim()}.
     */
    public double dot(Vector other) {
        final int sz = data.length;
        Preconditions.checkArgument(sz == other.dim());
        double s = 0;
        for (int i = 0; i < sz; i++) {
            s += data[i] * other.data[i];
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Vector) {
            return Arrays.equals(data, ((Vector) o).data);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
