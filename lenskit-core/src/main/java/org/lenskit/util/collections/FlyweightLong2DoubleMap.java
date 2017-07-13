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
