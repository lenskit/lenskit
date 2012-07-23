/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.Pointer;

import java.util.Iterator;

/**
 * Utility methods for interacting with vectors.
 */
public class Vectors {

    /**
     * Private constructor. This class is meant to be used
     * via its static methods, not instantiated.
     */
    private Vectors() {}

    public static Iterator<Long2DoubleMap.Entry[]> getPairedValsFast(SparseVector v1, SparseVector v2) {
        return new FastIteratorImpl(v1, v2);
    }

    public static Iterator<Long2DoubleMap.Entry[]> getPairedVals(SparseVector v1, SparseVector v2) {
        return new IteratorImpl(v1, v2);
    }



    private static final class IteratorImpl implements Iterator<Long2DoubleMap.Entry[]> {
        private boolean atNext = false;
        private final Pointer<Long2DoubleMap.Entry> p1;
        private final Pointer<Long2DoubleMap.Entry> p2;

        IteratorImpl(SparseVector v1, SparseVector v2) {
            p1 = CollectionUtils.pointer(v1.iterator());
            p2 = CollectionUtils.pointer(v2.iterator());
        }

        @Override
        public boolean hasNext() {
            if (!atNext) {
                while (!p1.isAtEnd() && !p2.isAtEnd()) {
                    long key1 = p1.get().getLongKey();
                    long key2 = p2.get().getLongKey();
                    if (key1 == key2) {
                        atNext = true;
                        break;
                    } else if (key1 < key2) {
                        p1.advance();
                    } else {
                        p2.advance();
                    }
                }
            }
            return atNext;
        }

        @Override
        public Long2DoubleMap.Entry[] next() {
            if (!hasNext()) {
                return null;
            }
            Long2DoubleMap.Entry[] curPair = {p1.get(), p2.get()};
            p1.advance();
            p2.advance();
            atNext = false;
            return curPair;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FastIteratorImpl implements Iterator<Long2DoubleMap.Entry[]> {
        private Entry[] curPair = new Entry[2];
        private boolean atNext = false;
        private final Pointer<Long2DoubleMap.Entry> p1;
        private final Pointer<Long2DoubleMap.Entry> p2;

        FastIteratorImpl(SparseVector v1, SparseVector v2) {
            curPair[0] = new Entry();
            curPair[1] = new Entry();
            p1 = CollectionUtils.pointer(v1.fastIterator());
            p2 = CollectionUtils.pointer(v2.fastIterator());
        }

        @Override
        public boolean hasNext() {
            if (!atNext) {
                while (!p1.isAtEnd() && !p2.isAtEnd()) {
                    long key1 = p1.get().getLongKey();
                    long key2 = p2.get().getLongKey();
                    if (key1 == key2) {
                        atNext = true;
                        break;
                    } else if (key1 < key2) {
                        p1.advance();
                    } else {
                        p2.advance();
                    }
                }
            }
            return atNext;
        }

        @Override
        public Long2DoubleMap.Entry[] next() {
            if (!hasNext()) {
                return null;
            }
            curPair[0].key = p1.get().getLongKey();
            curPair[0].value = p1.get().getDoubleValue();
            curPair[1].key = p2.get().getLongKey();
            curPair[1].value = p2.get().getDoubleValue();
            p1.advance();
            p2.advance();
            atNext = false;
            return curPair;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Wraps a (key, value) pair
     */
    public static final class Entry implements Long2DoubleMap.Entry {
        /**
         * Field writable only by Vectors.FastIteratorImpl
         */
        private long key;
        /**
         * Field writable only by Vectors.FastIteratorImpl
         */
        private double value;
        public long getLongKey() {
            return key;
        }
        public Long getKey() {
            return getLongKey();
        }
        public double getDoubleValue() {
            return value;
        }
        public Double getValue() {
            return getDoubleValue();
        }
        public double setValue(double value) {
            throw new UnsupportedOperationException();
        }
        public Double setValue(Double value) {
            throw new UnsupportedOperationException();
        }
    }

}
