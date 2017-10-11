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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.AbstractLong2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.function.LongToDoubleFunction;

/**
 * Flyweight map implementation.
 */
class FlyweightLong2DoubleMap extends AbstractLong2DoubleMap {
    private final LongSet keySet;
    private final LongToDoubleFunction valueFunc;

    public FlyweightLong2DoubleMap(LongSet keys, LongToDoubleFunction vf) {
        keySet = keys;
        valueFunc = vf;
    }

    @Override
    public int size() {
        return keySet.size();
    }

    @Override
    public ObjectSet<Entry> long2DoubleEntrySet() {
        return new EntrySet();
    }

    @Override
    public LongSet keySet() {
        return keySet;
    }

    @Override
    public boolean containsKey(long k) {
        return keySet.contains(k);
    }

    @Override
    public double get(long key) {
        return getOrDefault(key, defaultReturnValue());
    }

    @Override
    public double getOrDefault(long key, double defaultValue) {
        if (keySet.contains(key)) {
            return valueFunc.applyAsDouble(key);
        } else {
            return defaultValue;
        }
    }

    private class EntrySet extends AbstractObjectSet<Entry> implements FastEntrySet {
        @Override
        public ObjectIterator<Entry> fastIterator() {
            return new ObjectIterator<Entry>() {
                LongIterator iter = keySet.iterator();
                IndirectEntry e = new IndirectEntry();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry next() {
                    e.key = iter.nextLong();
                    return e;
                }
            };
        }

        @Override
        public ObjectIterator<Entry> iterator() {
            return new ObjectIterator<Entry>() {
                LongIterator iter = keySet.iterator();
                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry next() {
                    IndirectEntry e = new IndirectEntry();
                    e.key = iter.nextLong();
                    return e;
                }
            };
        }

        @Override
        public int size() {
            return keySet.size();
        }
    }

    private class IndirectEntry implements Entry {
        long key;

        @Override
        public long getLongKey() {
            return key;
        }

        @Override
        public Long getKey() {
            return key;
        }

        @Override
        public double getDoubleValue() {
            return valueFunc.applyAsDouble(key);
        }

        @Override
        public double setValue(double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Double getValue() {
            return getDoubleValue();
        }

        @Override
        public Double setValue(Double value) {
            throw new UnsupportedOperationException();
        }
    }
}
