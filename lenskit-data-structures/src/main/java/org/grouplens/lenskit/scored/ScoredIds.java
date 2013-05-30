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
package org.grouplens.lenskit.scored;

import org.grouplens.lenskit.collections.CopyingFastCollection;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import java.util.Iterator;

/**
 * Utility classes for working with {@linkplain ScoredId scored IDs}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 * @compat Experimental
 */
public final class ScoredIds {
    private ScoredIds() {}

    //region Vector conversion
    /**
     * View a vector as a {@link org.grouplens.lenskit.collections.FastCollection} of {@link ScoredId} objects.
     *
     * @return A fast collection containing this vector's keys and values as
     * {@link ScoredId} objects.
     * @param vector The vector to view as a collection of {@link ScoredId}s
     */
    public static FastCollection<ScoredId> collectionFromVector(SparseVector vector) {
        return new FastScoredIdCollectionImpl(vector);
    }

    private static class FastScoredIdCollectionImpl extends CopyingFastCollection<ScoredId> {

        private final SparseVector vector;

        public FastScoredIdCollectionImpl(SparseVector v) {
            vector = v;
        }

        @Override
        protected ScoredId copy(ScoredId elt) {
            ScoredIdBuilder builder = new ScoredIdBuilder();
            builder.setId(elt.getId());
            builder.setScore(elt.getScore());
            for (Symbol s: elt.getChannels()) {
                builder.addChannel(s, elt.channel(s));
            }

            return builder.build();
        }

        @Override
        public int size() {
            return vector.size();
        }

        @Override
        public Iterator<ScoredId> fastIterator() {
            return new FastIdIterImpl(vector);
        }
    }

    private static class FastIdIterImpl implements Iterator<ScoredId> {

        private final SparseVector vector;
        private Iterator<VectorEntry> entIter;
        private VectorEntryScoredId id;

        public FastIdIterImpl(SparseVector v) {
            vector = v;
            entIter = vector.fastIterator();
            id = new VectorEntryScoredId(vector);
        }

        @Override
        public boolean hasNext() {
            return entIter.hasNext();
        }

        @Override
        public ScoredId next() {
            id.setEntry(entIter.next());
            return id;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    //endregion
}
