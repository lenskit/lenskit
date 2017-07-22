/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
