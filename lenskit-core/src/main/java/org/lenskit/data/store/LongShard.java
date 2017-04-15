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
 * An object shard.
 */
abstract class LongShard extends Shard {

    private LongShard() {}

    /**
     * Create a new shard that may wrap a more compact storage.
     * @return The shard.
     */
    static LongShard create() {
        return new WrapShort();
    }

    /**
     * Create a new shard.
     * @return The shard.
     */
    static LongShard createFull() {
        return new Impl();
    }

    @Override
    Long get(int idx) {
        if (isNull(idx)) {
            return null;
        } else {
            return getLong(idx);
        }
    }

    abstract long getLong(int idx);

    @Override
    void put(int idx, Object value) {
        if (value == null) {
            clear(idx);
        } else if (value instanceof Long) {
            put(idx, ((Long) value).longValue());
        } else {
            throw new IllegalArgumentException("invalid value " + value);
        }
    }

    abstract void clear(int idx);

    abstract void put(int idx, long value);

    private void copyFrom(LongShard src) {
        int n = src.size();
        for (int i = 0; i < n; i++) {
            if (src.isNull(i)) {
                clear(i);
            } else {
                put(i, src.getLong(i));
            }
        }
    }

    private static class Impl extends LongShard {

        private long[] data = new long[SHARD_SIZE];
        private BitSet mask;
        private int size = 0;

        @Override
        long getLong(int idx) {
            assert idx >= 0 && idx < size;
            return data[idx];
        }

        @Override
        void clear(int idx) {
            assert idx >= 0 && idx < data.length;
            if (idx >= size) {
                size = idx + 1;
            }
            if (mask == null) {
                mask = new BitSet(SHARD_SIZE);
                mask.set(0, size);
            }
            mask.clear(idx);
        }

        @Override
        void put(int idx, long value) {
            assert idx >= 0 && idx < data.length;
            if (idx >= size) {
                if (idx > size && mask == null) {
                    mask = new BitSet(SHARD_SIZE);
                    mask.set(0, size);
                }
                size = idx + 1;
            }
            data[idx] = value;
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
        Shard adapt(Object obj) {
            if (obj instanceof Long) {
                return this;
            } else {
                throw new IllegalArgumentException("cannot store obj in long");
            }
        }

        @Override
        int size() {
            return size;
        }

        @Override
        void compact() {
            data = Arrays.copyOf(data, size);
        }
    }

    private static class WrapShort extends LongShard {
        private ShortShard delegate = ShortShard.create();

        @Override
        long getLong(int idx) {
            return delegate.getShort(idx);
        }

        @Override
        boolean isNull(int idx) {
            return delegate.isNull(idx);
        }

        @Override
        LongShard adapt(Object obj) {
            if (obj instanceof Long) {
                long val = (Long) obj;
                if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
                    return this;
                } else if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    LongShard lsh = new WrapInt();
                    lsh.copyFrom(this);
                    return lsh;
                } else {
                    LongShard lsh = createFull();
                    lsh.copyFrom(this);
                    return lsh;
                }
            } else {
                throw new IllegalArgumentException("cannot store " + obj + " in int shard");
            }
        }

        @Override
        void clear(int idx) {
            delegate.clear(idx);
        }

        @Override
        void put(int idx, long value) {
            assert value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
            delegate.put(idx, (short) value);
        }

        @Override
        int size() {
            return delegate.size();
        }

        @Override
        void compact() {
            delegate.compact();
        }
    }

    private static class WrapInt extends LongShard {
        private IntShard delegate = IntShard.createFull();

        @Override
        long getLong(int idx) {
            return delegate.getInt(idx);
        }

        @Override
        boolean isNull(int idx) {
            return delegate.isNull(idx);
        }

        @Override
        LongShard adapt(Object obj) {
            if (obj instanceof Long) {
                long val = (Long) obj;
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return this;
                } else {
                    LongShard lsh = createFull();
                    lsh.copyFrom(this);
                    return lsh;
                }
            } else {
                throw new IllegalArgumentException("cannot store " + obj + " in int shard");
            }
        }

        @Override
        void clear(int idx) {
            delegate.clear(idx);
        }

        @Override
        void put(int idx, long value) {
            assert value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
            delegate.put(idx, (int) value);
        }

        @Override
        int size() {
            return delegate.size();
        }

        @Override
        void compact() {
            delegate.compact();
        }
    }
}
