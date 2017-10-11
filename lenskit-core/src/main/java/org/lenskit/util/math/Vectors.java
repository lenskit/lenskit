/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.math;

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.DoubleUnaryOperator;

/**
 * Utility methods for vector arithmetic.
 */
public final class Vectors {
    private Vectors() {}

    /**
     * Get an iterator over the entries of the map. If possible, this is a {@linkplain Long2DoubleMap.FastEntrySet fast iterator}.
     *
     * @param map The map.
     * @return An iterator over the map's entries.
     */
    public static Iterator<Long2DoubleMap.Entry> fastEntryIterator(Long2DoubleMap map) {
        ObjectSet<Long2DoubleMap.Entry> entries = map.long2DoubleEntrySet();
        if (entries instanceof Long2DoubleMap.FastEntrySet) {
            return ((Long2DoubleMap.FastEntrySet) entries).fastIterator();
        } else {
            return entries.iterator();
        }
    }

    public static Iterable<Long2DoubleMap.Entry> fastEntries(final Long2DoubleMap map) {
        return new Iterable<Long2DoubleMap.Entry>() {
            @Override
            public Iterator<Long2DoubleMap.Entry> iterator() {
                return fastEntryIterator(map);
            }
        };
    }

    /**
     * Compute the sum of the elements of a map.
     * @param v The vector
     * @return The sum of the values of {@code v}.
     */
    public static double sum(Long2DoubleMap v) {
        double sum = 0;
        DoubleIterator iter = v.values().iterator();
        while (iter.hasNext()) {
            sum += iter.nextDouble();
        }
        return sum;
    }

    /**
     * Compute the sum of the elements of a map.
     * @param v The vector
     * @return The sum of the values of {@code v}.
     */
    public static double sumAbs(Long2DoubleMap v) {
        double sum = 0;
        DoubleIterator iter = v.values().iterator();
        while (iter.hasNext()) {
            sum += Math.abs(iter.nextDouble());
        }
        return sum;
    }

    /**
     * Compute the sum of the squares of elements of a map.
     * @param v The vector
     * @return The sum of the squares of the values of {@code v}.
     */
    public static double sumOfSquares(Long2DoubleMap v) {
        if (v instanceof Long2DoubleSortedArrayMap) {
            return sumOfSquares((Long2DoubleSortedArrayMap) v);
        } else {
            double sum = 0;
            DoubleIterator iter = v.values().iterator();
            while (iter.hasNext()) {
                double d = iter.nextDouble();
                sum += d * d;
            }
            return sum;
        }
    }

    /**
     * Compute the sum of the squares of elements of a map (optimized).
     * @param v The vector
     * @return The sum of the squares of the values of {@code v}.
     */
    public static double sumOfSquares(Long2DoubleSortedArrayMap v) {
        final int sz = v.size();
        double ssq = 0;
        for (int i = 0; i < sz; i++) {
            double val = v.getValueByIndex(i);
            ssq += val * val;
        }
        return ssq;
    }

    /**
     * Compute the Euclidean norm of the values of the map. This is the square root of the sum of squares.
     * @param v The vector.
     * @return The Euclidean norm of the vector.
     */
    public static double euclideanNorm(Long2DoubleMap v) {
        return Math.sqrt(sumOfSquares(v));
    }

    /**
     * Convert a vector to a unit vector.
     * @param v The vector.
     * @return A vector with Euclidean norm of 1.
     */
    public static Long2DoubleMap unitVector(Long2DoubleMap v) {
        return multiplyScalar(v, 1.0 / euclideanNorm(v));
    }

    /**
     * Compute the dot product of two maps. This method assumes any value missing in one map is 0, so it is the dot
     * product of the values of common keys.
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return The sum of the products of corresponding values in the two vectors.
     */
    public static double dotProduct(Long2DoubleMap v1, Long2DoubleMap v2) {
        if (v1.size() > v2.size()) {
            // compute dot product the other way around for speed
            return dotProduct(v2, v1);
        }

        if (v1 instanceof Long2DoubleSortedArrayMap && v2 instanceof Long2DoubleSortedArrayMap) {
            return dotProduct((Long2DoubleSortedArrayMap) v1, (Long2DoubleSortedArrayMap) v2);
        } else {
            double result = 0;

            Long2DoubleFunction v2d = adaptDefaultValue(v2, 0.0);
            Iterator<Long2DoubleMap.Entry> iter = fastEntryIterator(v1);
            while (iter.hasNext()) {
                Long2DoubleMap.Entry e = iter.next();
                long k = e.getLongKey();
                result += e.getDoubleValue() * v2d.get(k); // since default is 0
            }

            return result;
        }
    }

    /**
     * Compute the dot product of two maps (optimized). This method assumes any value missing in one map is 0, so it is the dot
     * product of the values of common keys.
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return The sum of the products of corresponding values in the two vectors.
     */
    public static double dotProduct(Long2DoubleSortedArrayMap v1, Long2DoubleSortedArrayMap v2) {
        double result;
        result = 0;
        final int sz1 = v1.size();
        final int sz2 = v2.size();

        int i1 = 0, i2 = 0;
        while (i1 < sz1 && i2 < sz2) {
            final long k1 = v1.getKeyByIndex(i1);
            final long k2 = v2.getKeyByIndex(i2);
            if (k1 < k2) {
                i1++;
            } else if (k2 < k1) {
                i2++;
            } else {
                result += v1.getValueByIndex(i1) * v2.getValueByIndex(i2);
                i1++;
                i2++;
            }
        }
        return result;
    }

    /**
     * Wrap a long-to-double function in another w/ the a different default return value.
     * @param f The function to wrap.
     * @param dft The new
     * @return A function with a different default value.
     */
    @SuppressWarnings("FloatingPointEquality")
    static Long2DoubleFunction adaptDefaultValue(final Long2DoubleFunction f, final double dft) {
        if (f.defaultReturnValue() == dft) {
            return f;
        }

        return new DftAdaptingL2DFunction(f, dft);
    }

    /**
     * Compute the arithmetic mean of a vector's values.
     * @param vec The vector.
     * @return The arithmetic mean.
     */
    public static double mean(Long2DoubleMap vec) {
        return sum(vec) / vec.size();
    }

    /**
     * Add a vector to another (scaled) vector and a scalar.  The result is \\(x_i + s_y y_i + o\\).
     *
     * @param x The source vector.
     * @param y The addition vector.  {@link Long2DoubleFunction#defaultReturnValue()} is assumed for missing values.
     * @param sy The scale by which elements of {@code y} are multipled.
     * @param o The offset to add.
     * @return A vector with the same keys as {@code x}, transformed by the specified linear formula.
     */
    public static Long2DoubleMap combine(Long2DoubleMap x, Long2DoubleFunction y, double sy, double o) {
        SortedKeyIndex idx = SortedKeyIndex.fromCollection(x.keySet());
        final int n = idx.size();
        double[] values = new double[n];

        if (x instanceof Long2DoubleSortedArrayMap) {
            // TODO make this fast for two sorted maps
            Long2DoubleSortedArrayMap sx = (Long2DoubleSortedArrayMap) x;
            assert idx == sx.keySet().getIndex();
            for (int i = 0; i < n; i++) {
                values[i] = sx.getValueByIndex(i) + y.get(idx.getKey(i)) * sy + o;
            }
        } else {
            for (int i = 0; i < n; i++) {
                long k = idx.getKey(i);
                values[i] = x.get(k) + y.get(k) * sy + o;
            }
        }

        return Long2DoubleSortedArrayMap.wrap(idx, values);
    }

    /**
     * Add a scalar to each element of a vector.
     * @param vec The vector to rescale.
     * @param val The value to add.
     * @return A new map with every value in {@code vec} increased by {@code val}.
     */
    public static Long2DoubleMap addScalar(Long2DoubleMap vec, double val) {
        SortedKeyIndex keys = SortedKeyIndex.fromCollection(vec.keySet());
        final int n = keys.size();
        double[] values = new double[n];
        if (vec instanceof Long2DoubleSortedArrayMap) {
            Long2DoubleSortedArrayMap sorted = (Long2DoubleSortedArrayMap) vec;
            for (int i = 0; i < n; i++) {
                assert sorted.getKeyByIndex(i) == keys.getKey(i);
                values[i] = sorted.getValueByIndex(i) + val;
            }
        } else {
            for (int i = 0; i < n; i++) {
                values[i] = vec.get(keys.getKey(i)) + val;
            }
        }

        return Long2DoubleSortedArrayMap.wrap(keys, values);
    }

    /**
     * Multiply each element of a vector by a scalar.
     * @param vector The vector.
     * @param value The scalar to multiply.
     * @return A new vector consisting of the same keys as `vector`, with `value` multipled by each.
     */
    @Nonnull
    public static Long2DoubleMap multiplyScalar(Long2DoubleMap vector, double value) {
        // TODO Consier implementing this in terms of transform
        SortedKeyIndex idx = SortedKeyIndex.fromCollection(vector.keySet());
        int n = idx.size();
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = vector.get(idx.getKey(i)) * value;
        }

        return Long2DoubleSortedArrayMap.wrap(idx, values);
    }

    /**
     * Transform the values of a vector.
     *
     * @param input The vector to transform.
     * @param function The transformation to apply.
     * @return A new vector that is the result of applying `function` to each value in `input`.
     */
    public static Long2DoubleMap transform(Long2DoubleMap input, DoubleUnaryOperator function) {
        // FIXME Improve performance when input is also sorted
        SortedKeyIndex idx = SortedKeyIndex.fromCollection(input.keySet());
        int n = idx.size();
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = function.applyAsDouble(input.get(idx.getKey(i)));
        }

        return Long2DoubleSortedArrayMap.wrap(idx, values);
    }

    /**
     * Create a flyweight row view of a matrix.
     * @param mat The matrix.
     * @param row The row number.
     * @return A vector that is a read-only flyweight view of the matrix row.
     */
    public static RealVector matrixRow(RealMatrix mat, int row) {
        if (row < 0 || row >= mat.getRowDimension()) {
            throw new OutOfRangeException(row, 0, mat.getRowDimension());
        }
        return new RowView(mat, row);
    }

    private static class DftAdaptingL2DFunction implements Long2DoubleFunction {
        private final Long2DoubleFunction delegate;
        private final double dft;

        public DftAdaptingL2DFunction(Long2DoubleFunction f, double dft) {
            this.delegate = f;
            this.dft = dft;
        }

        @Override
        public double get(long l) {
            if (delegate.containsKey(l)) {
                return delegate.get(l);
            } else {
                return dft;
            }
        }

        @Override
        public boolean containsKey(long l) {
            return delegate.containsKey(l);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public double put(long key, double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double remove(long key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void defaultReturnValue(double rv) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double defaultReturnValue() {
            return dft;
        }

        @Override
        public Double put(Long key, Double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Double get(Object key) {
            return delegate.get(key);
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public Double remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }
}
