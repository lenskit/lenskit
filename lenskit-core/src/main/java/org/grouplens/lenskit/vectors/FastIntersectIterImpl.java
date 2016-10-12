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
package org.grouplens.lenskit.vectors;

import it.unimi.dsi.fastutil.ints.IntIterator;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.NoSuchElementException;

class FastIntersectIterImpl implements Iterator<Pair<VectorEntry, VectorEntry>> {
    private boolean atNext = false;
    private final SparseVector vec1, vec2;
    private IntIterator iterA, iterB;
    // indexes, or -1 for exhausted iterators
    private int idxA, idxB;
    private VectorEntry leftEnt;
    private VectorEntry rightEnt;
    private MutablePair<VectorEntry, VectorEntry> pair;

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
