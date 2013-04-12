package org.grouplens.lenskit.vectors;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Immutable {@link Vector}.  This vector cannot be modified (by anyone) and is thread-safe.
 *
 * @compat Experimental — this interface may change in future versions of LensKit.
 */
@Immutable
public class ImmutableVector extends Vector {
    ImmutableVector(double[] v) {
        super(v);
    }

    /**
     * Create a new vector from data in an array.  The array is copied for safety.
     *
     * @param data The data array.
     * @param length The number of elements to use, starting from the first.
     * @return A vector containing the data in {@code data}.
     */
    public static ImmutableVector make(double[] data, int length) {
        Preconditions.checkArgument(data.length >= length, "length mismatch");
        return new ImmutableVector(Arrays.copyOf(data, length));
    }

    /**
     * Create a new vector from data in an array.  The array is copied for safety.
     *
     * @param data The data array.
     * @return A vector containing the data in {@code data}.
     */
    public static ImmutableVector make(double[] data) {
        return make(data, data.length);
    }

    @Override
    public ImmutableVector immutable() {
        return this;
    }
}
