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
package org.lenskit.data.store;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A shard of doubles.
 */
abstract class DoubleShard extends Shard {
    protected BitSet mask;
    protected int size = 0;

    private DoubleShard() {}

    static DoubleShard create() {
        return new Compact();
    }

    @Override
    Double get(int idx) {
        assert idx >= 0 && idx < size;
        if (mask == null || mask.get(idx)) {
            return getDouble(idx);
        } else {
            return null;
        }
    }

    @Override
    void put(int idx, Object value) {
        if (value == null) {
            clear(idx);
        } else if (value instanceof Double) {
            put(idx, ((Double) value).doubleValue());
        } else {
            throw new IllegalArgumentException("invalid value " + value);
        }
    }

    void clear(int idx) {
        assert idx >= 0 && idx < capacity();
        if (idx >= size) {
            size = idx + 1;
        }
        if (mask == null) {
            mask = new BitSet(SHARD_SIZE);
            mask.set(0, size);
        }
        mask.clear(idx);
    }

    void put(int idx, double value) {
        assert idx >= 0 && idx < capacity();
        if (idx >= size) {
            if (idx > size && mask == null) {
                mask = new BitSet(SHARD_SIZE);
                mask.set(0, size);
            }
            size = idx + 1;
        }
        putDouble(idx, value);
        if (mask != null) {
            mask.set(idx);
        }
    }

    @Override
    boolean isNull(int idx) {
        assert idx >= 0 && idx < size;
        return mask != null && !mask.get(idx);
    }

    @Override
    int size() {
        return size;
    }

    abstract int capacity();

    abstract double getDouble(int idx);
    abstract void putDouble(int idx, double v);
    @Override
    abstract DoubleShard adapt(Object v);

    private static class Full extends DoubleShard {
        private double[] data = new double[SHARD_SIZE];

        double getDouble(int idx) {
            assert idx >= 0 && idx < size;
            return data[idx];
        }

        @Override
        void putDouble(int idx, double v) {
            data[idx] = v;
        }

        @Override
        void compact() {
            data = Arrays.copyOf(data, size);
        }

        @Override
        int capacity() {
            return data.length;
        }

        @Override
        DoubleShard adapt(Object obj) {
            if (obj instanceof Double || obj == null) {
                return this;
            } else {
                throw new IllegalArgumentException("cannot store obj in double");
            }
        }
    }

    /**
     * Fixed-point storage for values with precision of 0.5.
     */
    static class Compact extends DoubleShard {
        private byte[] data = new byte[SHARD_SIZE];

        double getDouble(int idx) {
            assert idx >= 0 && idx < size;
            double v = data[idx];
            return v / 2;
        }

        @Override
        void putDouble(int idx, double v) {
            assert isStorable(v);
            assert idx >= 0 && idx < size;
            double ri = Math.rint(v * 2);
            data[idx] = (byte) ri;
        }

        @Override
        void compact() {
            data = Arrays.copyOf(data, size);
        }

        @Override
        int capacity() {
            return data.length;
        }

        static boolean isStorable(double v) {
            // WARNING here lies IEEE 754 bit-bashing
            long bits = Double.doubleToRawLongBits(v);
            int e = (int)((bits >> 52) & 0x7ffL) - 1023;
            long f = (bits & 0xfffffffffffffL);
            if (e == -1023 && f == 0) {
                // zero
                return true;
            } else if (e < -1 || e > 5) {
                // out of range (-64,64)
                return false;
            } else {
                // in range (-64,64); is it at appropriate resolution?
                // f is 52 bits long; only the first e+1 can be set
                long m2 = (f >> (51-e)) << (51-e);
                return m2 == f;
            }
        }

        @Override
        DoubleShard adapt(Object obj) {
            if (obj == null) {
                return this;
            } else if (obj instanceof Double) {
                double v = (double) obj;
                if (isStorable(v)) {
                    return this;
                } else {
                    Full full = new Full();
                    int n = size;
                    full.size = n;
                    full.mask = mask != null ? (BitSet) mask.clone() : null;
                    for (int i = 0; i < n; i++) {
                        full.data[i] = data[i] * 0.5;
                    }
                    return full;
                }
            } else {
                throw new IllegalArgumentException("cannot store obj in double");
            }
        }
    }
}
