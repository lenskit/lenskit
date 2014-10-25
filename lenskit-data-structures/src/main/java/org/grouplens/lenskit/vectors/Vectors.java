/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.indexes.IdIndexMapping;
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
        for (ScoredId sid: scores) {
            ids.add(sid.getId());
        }
        MutableSparseVector vec = MutableSparseVector.create(ids);
        for (ScoredId sid: scores) {
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

    /**
     * Create a mutable sparse vector from an array and index mapping.
     *
     * @param map The index mapping specifying the keys.
     * @param values The array of values.
     * @return A sparse vector mapping the IDs in {@code map} to the values in {@code values}.
     * @throws IllegalArgumentException if {@code values} not the same size as {@code map}.
     */
    public static MutableSparseVector fromArray(IdIndexMapping map, double[] values) {
        Preconditions.checkArgument(values.length == map.size(),
                                    "values array longer than id mapping");
        MutableSparseVector msv = MutableSparseVector.create(map.getIdList());
        for (VectorEntry e: msv.fast(VectorEntry.State.EITHER)) {
            msv.set(e, values[map.getIndex(e.getKey())]);
        }
        return msv;
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
        private IntIterator iterA, iterB;
        // indexes, or -1 for exhausted iterators
        private int idxA, idxB;
        private VectorEntry leftEnt;
        private VectorEntry rightEnt;
        private MutablePair<VectorEntry,VectorEntry> pair;

        public FastIntersectIterImpl(SparseVector v1, SparseVector v2) {
            vec1 = v1;
            vec2 = v2;
            // FIXME The true here slows things down
            iterA = v1.keys.activeIndexIterator(true);
            iterB = v2.keys.activeIndexIterator(true);
            idxA = iterA.hasNext() ? iterA.nextInt() : -1;
            idxB = iterB.hasNext() ? iterB.nextInt() : -1;
            leftEnt = new VectorEntry(v1, -1, 0, 0, false);
            rightEnt = new VectorEntry(v2, -1, 0, 0, false);
            pair = MutablePair.of(leftEnt, rightEnt);
        }

        @Override
        public boolean hasNext() {
            if (!atNext) {
                while (idxA >= 0 && idxB >= 0) {
                    long ka = vec1.keys.getKey(idxA);
                    long kb = vec2.keys.getKey(idxB);
                    if (ka == kb) {
                        atNext = true;
                        break;
                    } else if (ka < kb) {
                        idxA = iterA.hasNext() ? iterA.nextInt() : -1;
                    } else {
                        idxB = iterB.hasNext() ? iterB.nextInt() : -1;
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

            assert vec1.keys.getKey(idxA) == vec2.keys.getKey(idxB);

            leftEnt.set(idxA, vec1.keys.getKey(idxA), vec1.values[idxA], true);
            idxA = iterA.hasNext() ? iterA.nextInt() : -1;

            rightEnt.set(idxB, vec2.keys.getKey(idxB), vec2.values[idxB], true);
            idxB = iterB.hasNext() ? iterB.nextInt() : -1;

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
