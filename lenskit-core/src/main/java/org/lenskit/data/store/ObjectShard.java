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

/**
 * An object shard.
 */
class ObjectShard extends Shard {
    private Object[] data = new Object[SHARD_SIZE];
    private int size = 0;

    @Override
    Object get(int idx) {
        assert idx >= 0 && idx < size;
        return data[idx];
    }

    @Override
    void put(int idx, Object value) {
        assert idx >= 0 && idx < data.length;
        if (idx >= size) {
            size = idx + 1;
        }
        data[idx] = value;
    }

    @Override
    boolean isNull(int idx) {
        assert idx >= 0 && idx < size;
        return data[idx] == null;
    }

    @Override
    Shard adapt(Object obj) {
        return this;
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
