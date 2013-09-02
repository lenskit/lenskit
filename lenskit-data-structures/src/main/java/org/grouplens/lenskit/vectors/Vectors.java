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
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.IntPointer;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.symbols.TypedSymbol;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static MutableSparseVector fromScoredIds(List<ScoredId> scores) {
        LongSet ids = new LongOpenHashSet();
        for (ScoredId sid: CollectionUtils.fast(scores)) {
            ids.add(sid.getId());
        }
        MutableSparseVector vec = MutableSparseVector.create(ids);
        for (ScoredId sid: CollectionUtils.fast(scores)) {
            long id = sid.getId();
            if (!vec.containsKey(id)) {
                vec.set(id, sid.getScore());
                for (Symbol sym: sid.getUnboxedChannelSymbols()) {
                    vec.getOrAddChannelVector(sym).set(id, sid.getUnboxedChannelValue(sym));
                }
                for (TypedSymbol tsym: sid.getChannelSymbols()) {
                    if (!tsym.getType().equals(Double.class)) {
                        vec.getOrAddChannel(tsym).put(id, sid.getChannelValue(tsym));
                    }
                }
            }
        }
        return vec;
    }

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
        private final SparseVector vec1, vec2;
        private IntPointer p1, p2;
        private VectorEntry leftEnt;
        private VectorEntry rightEnt;
        private MutablePair<VectorEntry,VectorEntry> pair;

        public FastIntersectIterImpl(SparseVector v1, SparseVector v2) {
            vec1 = v1;
            vec2 = v2;
            p1 = v1.keys.activeIndexPointer(false);
            p2 = v2.keys.activeIndexPointer(false);
            leftEnt = new VectorEntry(v1, -1, 0, 0, false);
            rightEnt = new VectorEntry(v2, -1, 0, 0, false);
            pair = MutablePair.of(leftEnt, rightEnt);
        }

        @Override
        public boolean hasNext() {
            if (!atNext) {
                while (!p1.isAtEnd() && !p2.isAtEnd()) {
                    long key1 = vec1.keys.getKey(p1.getInt());
                    long key2 = vec2.keys.getKey(p2.getInt());
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

            final int i1 = p1.getInt();
            final int i2 = p2.getInt();
            assert vec1.keys.getKey(i1) == vec2.keys.getKey(i2);

            leftEnt.set(i1, vec1.keys.getKey(i1), vec1.values[i1], true);
            p1.advance();

            rightEnt.set(i2, vec2.keys.getKey(i2), vec2.values[i2], true);
            p2.advance();

            atNext = false;

            return pair;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    //endregion
}
