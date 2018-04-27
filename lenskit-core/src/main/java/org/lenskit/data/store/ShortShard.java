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
 * An object shard.
 */
class ShortShard extends Shard {
    private short[] data = new short[SHARD_SIZE];
    private BitSet mask;
    private int size = 0;

    private ShortShard() {}

    static ShortShard create() {
        return new ShortShard();
    }

    @Override
    Short get(int idx) {
        assert idx >= 0 && idx < size;
        if (mask == null || mask.get(idx)) {
            return getShort(idx);
        } else {
            return null;
        }
    }

    short getShort(int idx) {
        assert idx >= 0 && idx < size;
        return data[idx];
    }

    @Override
    void put(int idx, Object value) {
        if (value == null) {
            clear(idx);
        } else if (value instanceof Short) {
            put(idx, ((Short) value).shortValue());
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

    void put(int idx, short value) {
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
        if (obj instanceof Short || obj == null) {
            return this;
        } else {
            throw new IllegalArgumentException("cannot store obj in short");
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
