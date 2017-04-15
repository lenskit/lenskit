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
class IntShard extends Shard {
    private int[] data = new int[SHARD_SIZE];
    private BitSet mask;
    private int size = 0;

    private IntShard() {}

    static IntShard create() {
        return new IntShard();
    }

    @Override
    Integer get(int idx) {
        assert idx >= 0 && idx < size;
        if (mask == null || mask.get(idx)) {
            return getInt(idx);
        } else {
            return null;
        }
    }

    int getInt(int idx) {
        assert idx >= 0 && idx < size;
        return data[idx];
    }

    @Override
    void put(int idx, Object value) {
        if (value == null) {
            clear(idx);
        } else if (value instanceof Integer) {
            put(idx, ((Integer) value).intValue());
        } else {
            throw new IllegalArgumentException("invalid value " + value);
        }
    }

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

    void put(int idx, int value) {
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
            throw new IllegalArgumentException("cannot store obj in int");
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
