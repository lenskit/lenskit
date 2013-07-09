/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.vectors;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.Pointer;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Utility methods for interacting with vectors.
 *
 * @compat Public
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Vectors {
    /**
     * Private constructor. This class is meant to be used
     * via its static methods, not instantiated.
     */
    private Vectors() {}

    //region Paired iteration
    private static final Function<Pair<VectorEntry,VectorEntry>, ImmutablePair<VectorEntry,VectorEntry>>
            IMMUTABLE_PAIR_COPY = new Function<Pair<VectorEntry, VectorEntry>,
                                               ImmutablePair<VectorEntry, VectorEntry>>() {

        @Nullable
        @Override
        public ImmutablePair<VectorEntry, VectorEntry> apply(@Nullable Pair<VectorEntry, VectorEntry> p) {
            if (p == null) {
                return null;
            } else {
                VectorEntry left = p.getLeft();
                if (left != null) {
                    left = left.clone();
                }
                VectorEntry right = p.getRight();
                if (right != null) {
                    right = right.clone();
                }
                return ImmutablePair.of(left, right);
            }
        }
    };

    /**
     * Iterate over the union of two vectors (the keys set in either).  Each pair in the resulting
     * iterable contains the entries for a key appearing in one or both vectors.  If a key is only
     * in one vector's key set, that vector entry is returned (as {@link Pair#getLeft()} for v1 and
     * {@link Pair#getRight()} for v2) and the other is null.  If the key is in both vector's key
     * sets, both entries are returned.  The key domains are not handled separately; this method
     * does <emph>not</emph> return entries that appear in a vector's key domain but neither vector's
     * key set.
     *
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return An iterable of pairs of entries.
     */
    public static Iterable<ImmutablePair<VectorEntry,VectorEntry>> union(final SparseVector v1, final SparseVector v2) {
        return new Iterable<ImmutablePair<VectorEntry, VectorEntry>>() {
            @Override
            public Iterator<ImmutablePair<VectorEntry, VectorEntry>> iterator() {
                return Iterators.transform(new FastUnionIterImpl(v1, v2), IMMUTABLE_PAIR_COPY);
            }
        };
    }

    /**
     * A fast iterator version of {@link #union(SparseVector, SparseVector)}.
     *
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return A fast iteratable of pairs of vector entries.
     * @see #union(SparseVector, SparseVector)
     */
    public static Iterable<Pair<VectorEntry,VectorEntry>> fastUnion(final SparseVector v1, final SparseVector v2) {
        return new Iterable<Pair<VectorEntry, VectorEntry>>() {
            @Override
            public Iterator<Pair<VectorEntry, VectorEntry>> iterator() {
                return new FastUnionIterImpl(v1, v2);
            }
        };
    }

    private static class FastUnionIterImpl implements Iterator<Pair<VectorEntry,VectorEntry>> {

        private Pointer<VectorEntry> p1;
        private Pointer<VectorEntry> p2;
        private VectorEntry leftEnt;
        private VectorEntry rightEnt;
        private MutablePair<VectorEntry,VectorEntry> pair;

        public FastUnionIterImpl(SparseVector v1, SparseVector v2) {
            p1 = v1.fastPointer();
            p2 = v2.fastPointer();
            leftEnt = new VectorEntry(v1, -1, 0, 0, false);
            rightEnt = new VectorEntry(v2, -1, 0, 0, false);
            pair = new MutablePair<VectorEntry,VectorEntry>(leftEnt, rightEnt);
        }

        @Override
        public boolean hasNext() {
            return !p1.isAtEnd() || !p2.isAtEnd();
        }

        @Override
        public Pair<VectorEntry, VectorEntry> next() {
            if (!p1.isAtEnd() && !p2.isAtEnd()) {
                final VectorEntry e1 = p1.get();
                final VectorEntry e2 = p2.get();
                final long k1 = e1.getKey();
                final long k2 = e2.getKey();
                if (k1 < k2) {
                    leftEnt.set(e1.getIndex(), k1, e1.getValue(), e1.isSet());
                    pair.setLeft(leftEnt);
                    p1.advance();

                    pair.setRight(null);

                    return pair;
                } else if (k2 < k1) {
                    pair.setLeft(null);

                    rightEnt.set(e2.getIndex(), k2, e2.getValue(), e2.isSet());
                    pair.setRight(rightEnt);
                    p2.advance();

                    return pair;
                } else {
                    leftEnt.set(e1.getIndex(), k1, e1.getValue(), e1.isSet());
                    pair.setLeft(leftEnt);
                    p1.advance();

                    rightEnt.set(e2.getIndex(), k2, e2.getValue(), e2.isSet());
                    pair.setRight(rightEnt);
                    p2.advance();

                    return pair;
                }
            } else if (!p1.isAtEnd()) {
                VectorEntry e1 = p1.get();
                leftEnt.set(e1.getIndex(), e1.getKey(), e1.getValue(), e1.isSet());
                pair.setLeft(leftEnt);
                p1.advance();

                pair.setRight(null);

                return pair;
            } else if (!p2.isAtEnd()) {
                pair.setLeft(null);

                VectorEntry e2 = p2.get();
                rightEnt.set(e2.getIndex(), e2.getKey(), e2.getValue(), e2.isSet());
                pair.setRight(rightEnt);
                p2.advance();

                return pair;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Iterate over the intersection of two vectors - they keys they have in common.
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return An iterator over the common pairs. This iterator will never contain null entries.
     */
    public static Iterable<ImmutablePair<VectorEntry,VectorEntry>> intersect(final SparseVector v1, final SparseVector v2) {
        return new Iterable<ImmutablePair<VectorEntry, VectorEntry>>() {
            @Override
            public Iterator<ImmutablePair<VectorEntry, VectorEntry>> iterator() {
                return Iterators.transform(new FastIntersectIterImpl(v1, v2), IMMUTABLE_PAIR_COPY);
            }
        };
    }

    /**
     * Iterate over the intersection of two vectors without the overhead of object creation.
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return A fast iterator over the common keys of the two vectors.
     * @see #intersect(SparseVector, SparseVector)
     */
    public static Iterable<Pair<VectorEntry,VectorEntry>> fastIntersect(final SparseVector v1, final SparseVector v2) {
        return new Iterable<Pair<VectorEntry, VectorEntry>>() {
            @Override
            public Iterator<Pair<VectorEntry, VectorEntry>> iterator() {
                return new FastIntersectIterImpl(v1, v2);
            }
        };
    }

    private static class FastIntersectIterImpl implements Iterator<Pair<VectorEntry,VectorEntry>> {
        private boolean atNext = false;
        private Pointer<VectorEntry> p1;
        private Pointer<VectorEntry> p2;
        private VectorEntry leftEnt;
        private VectorEntry rightEnt;
        private MutablePair<VectorEntry,VectorEntry> pair;

        public FastIntersectIterImpl(SparseVector v1, SparseVector v2) {
            p1 = v1.fastPointer();
            p2 = v2.fastPointer();
            leftEnt = new VectorEntry(v1, -1, 0, 0, false);
            rightEnt = new VectorEntry(v2, -1, 0, 0, false);
            pair = MutablePair.of(leftEnt, rightEnt);
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
        public Pair<VectorEntry, VectorEntry> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final VectorEntry e1 = p1.get();
            final VectorEntry e2 = p2.get();
            assert e1.getKey() == e2.getKey();

            leftEnt.set(e1.getIndex(), e1.getKey(), e1.getValue(), e1.isSet());
            p1.advance();

            rightEnt.set(e2.getIndex(), e2.getKey(), e2.getValue(), e2.isSet());
            p2.advance();

            atNext = false;

            return pair;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Provides an Iterable over EntryPairs based off of a fast Iterator.
     *
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return an Iterable<EntryPair> wrapping a fast Iterator.
     * @deprecated This can be implemented in terms of {@link #fastUnion(SparseVector, SparseVector)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static Iterable<EntryPair> pairedFast(final SparseVector v1, final SparseVector v2) {
        return new Iterable<EntryPair>() {
            @Override
            public Iterator<EntryPair> iterator() {
                return pairedFastIterator(v1, v2);
            }
        };
    }

    /**
     * Returns a fast Iterator over the value pairs of the parameter
     * SparseVectors that share common keys.
     *
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return a fast Iterator over EntryPairs, representing a shared
     *         key and the paired values for that key.
     * @deprecated This can be implemented in terms of {@link #fastUnion(SparseVector, SparseVector)}
     */
    @Deprecated
    public static Iterator<EntryPair> pairedFastIterator(SparseVector v1, SparseVector v2) {
        return new FastIteratorImpl(v1, v2);
    }

    /**
     * Provides an Iterable over EntryPairs.
     *
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return an Iterable<EntryPair> wrapping a fast Iterator.
     * @deprecated This can be implemented in terms of {@link #union(SparseVector, SparseVector)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
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
     *
     * @param v1 a SparseVector
     * @param v2 a SparseVector
     * @return an Iterator over EntryPairs, representing a shared
     *         key and the paired values for that key.
     * @deprecated This can be implemented in terms of {@link #union(SparseVector, SparseVector)}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static Iterator<EntryPair> pairedIterator(SparseVector v1, SparseVector v2) {
        return new IteratorImpl(v1, v2);
    }

    @SuppressWarnings("deprecation")
    private static final class IteratorImpl implements Iterator<EntryPair> {
        private boolean atNext = false;
        private final Pointer<VectorEntry> p1;
        private final Pointer<VectorEntry> p2;

        IteratorImpl(SparseVector v1, SparseVector v2) {
            p1 = v1.fastPointer();
            p2 = v2.fastPointer();
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

    @SuppressWarnings("deprecation")
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
     * @deprecated Use {@link #intersect(SparseVector, SparseVector)}.
     */
    @Deprecated
    public static final class EntryPair {
        private long key;
        private double value1;
        private double value2;

        /**
         * Construct an entry pair with zero key and values.
         */
        EntryPair() {}

        /**
         * Construct an entry pair with a key & values.
         *
         * @param k  The key.
         * @param v1 The first vector's value for {@var key}.
         * @param v2 The second vector's value for {@var key}.
         */
        public EntryPair(long k, double v1, double v2) {
            key = k;
            value1 = v1;
            value2 = v2;
        }

        /**
         * Get the pair's key.
         *
         * @return The key.
         */
        public Long getKey() {
            return key;
        }

        /**
         * Get the first vector's value.
         *
         * @return The first vector's value for the {@link #getKey() key}.
         */
        public double getValue1() {
            return value1;
        }

        /**
         * Get the second vector's value.
         *
         * @return The second vector's value for the {@link #getKey() key}.
         */
        public Double getValue2() {
            return value2;
        }
    }
    //endregion
}
