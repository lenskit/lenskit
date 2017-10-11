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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleIterators;
import it.unimi.dsi.fastutil.longs.AbstractLong2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

/**
 * Wrapper for long-to-double functions.
 */
class Long2DoubleMapWrapper implements Long2DoubleMap {

    private final Map<Long, Double> map;
    private double defaultReturnValue = 0;

    public Long2DoubleMapWrapper(Map<Long, Double> map) {
        this.map = map;
    }

    @Override
    public double get(long l) {
        Double d = map.get(l);
        if (d == null) {
            return defaultReturnValue();
        } else {
            return d;
        }
    }

    @Override
    public double put(long key, double value) {
        Double rv = map.put(key, value);
        return rv != null ? rv : defaultReturnValue();
    }

    @Override
    public Double put(Long ok, Double ov) {
        return map.put(ok, ov);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public double remove(long l) {
        Double v = map.remove(l);
        if (v == null) {
            return defaultReturnValue();
        } else {
            return v;
        }
    }

    @Override
    public boolean containsKey(long l) {
        return map.containsKey(l);
    }

    @Override
    public void defaultReturnValue(double v) {
        defaultReturnValue = v;
    }

    @Override
    public double defaultReturnValue() {
        return defaultReturnValue;
    }

    @Override
    public Double get(Object o) {
        return map.get(o);
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Double remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public ObjectSet<Map.Entry<Long, Double>> entrySet() {
        return null;
    }

    @Override
    public ObjectSet<Entry> long2DoubleEntrySet() {
        return new AbstractObjectSet<Entry>() {
            @Override
            public ObjectIterator<Entry> iterator() {
                Iterator<Entry> iter =
                        Iterators.transform(map.entrySet().iterator(),
                                            new Function<Map.Entry<Long, Double>, Entry>() {
                                                @Nullable
                                                @Override
                                                public Entry apply(@Nullable Map.Entry<Long, Double> input) {
                                                    if (input == null) {
                                                        return null;
                                                    }
                                                    return new AbstractLong2DoubleMap.BasicEntry(input.getKey(), input.getValue());
                                                }
                                            });
                return ObjectIterators.asObjectIterator(iter);
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    @Override
    public LongSet keySet() {
        return LongUtils.asLongSet(map.keySet());
    }

    @Override
    public DoubleCollection values() {
        return new AbstractDoubleCollection() {
            @Override
            public DoubleIterator iterator() {
                return DoubleIterators.asDoubleIterator(map.values().iterator());
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    @Override
    public boolean containsValue(double value) {
        return map.containsValue(value);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public void putAll(Map<? extends Long, ? extends Double> m) {
        map.putAll(m);
    }
}
