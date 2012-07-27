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

    /**
     * Provides an Iterable over EntryPairs based off of a fast Iterator.
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return an Iterable<EntryPair> wrapping a fast Iterator.
     */
    public static Iterable<EntryPair> pairedFast(final SparseVector v1, final SparseVector v2) {
        return new Iterable<EntryPair>() {
            @Override
            public Iterator<EntryPair> iterator() {
                return pairedIteratorFast(v1, v2);
            }
        };
    }

    /**
     * Returns a fast Iterator over the value pairs of the parameter
     * SparseVectors that share common keys.
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return a fast Iterator over EntryPairs, representing a shared
     * key and the paired values for that key.
     */
    public static Iterator<EntryPair> pairedIteratorFast(SparseVector v1, SparseVector v2) {
        return new FastIteratorImpl(v1, v2);
    }

    /**
     * Provides an Iterable over EntryPairs.
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return an Iterable<EntryPair> wrapping a fast Iterator.
     */
    public static Iterable<EntryPair> paired(final SparseVector v1, final SparseVector v2) {
        return new Iterable<EntryPair>() {
            @Override
            public Iterator<EntryPair> iterator() {
                return pairedIterator(v1, v2);
            }
        };
    }

    /**
     * Returns an Iterator over the value pairs of the parameter
     * SparseVectors that share common keys.
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return an Iterator over EntryPairs, representing a shared
     * key and the paired values for that key.
     */
    public static Iterator<EntryPair> pairedIterator(SparseVector v1, SparseVector v2) {
        return new IteratorImpl(v1, v2);
    }



    private static final class IteratorImpl implements Iterator<EntryPair> {
        private boolean atNext = false;
        private final Pointer<VectorEntry> p1;
        private final Pointer<VectorEntry> p2;

        IteratorImpl(SparseVector v1, SparseVector v2) {
            p1 = CollectionUtils.pointer(v1.iterator());
            p2 = CollectionUtils.pointer(v2.iterator());
        }

        @Override
        public boolean hasNext() {
            if (!atNext) {
                while (!p1.isAtEnd() && !p2.isAtEnd()) {
                    long key1 = p1.get().getKey();
                    long key2 = p2.get().getKey();
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
        public EntryPair next() {
            if (!hasNext()) {
                return null;
            }
            EntryPair curPair = new EntryPair(p1.get().getKey(), p1.get().getValue(), p2.get().getValue());
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

    private static final class FastIteratorImpl implements Iterator<EntryPair> {
        private EntryPair curPair = new EntryPair();
        private boolean atNext = false;
        private final Pointer<VectorEntry> p1;
        private final Pointer<VectorEntry> p2;

        FastIteratorImpl(SparseVector v1, SparseVector v2) {
            p1 = CollectionUtils.pointer(v1.fastIterator());
            p2 = CollectionUtils.pointer(v2.fastIterator());
        }

        @Override
        public boolean hasNext() {
            if (!atNext) {
                while (!p1.isAtEnd() && !p2.isAtEnd()) {
                    long key1 = p1.get().getKey();
                    long key2 = p2.get().getKey();
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
        public EntryPair next() {
            if (!hasNext()) {
                return null;
            }
            curPair.key = p1.get().getKey();
            curPair.value1 = p1.get().getValue();
            curPair.value2 = p2.get().getValue();
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
     * Wraps a pair of values that share a common key.
     */
    public static final class EntryPair {
        long key;
        double value1;
        double value2;

        public EntryPair() {}
        public EntryPair(long key, double value1, double value2) {
            this.key = key;
            this.value1 = value1;
            this.value2 = value2;
        }
        public Long getKey() {
            return key;
        }
        public double getValue1() {
            return value1;
        }
        public Double getValue2() {
            return value2;
        }
    }

}
