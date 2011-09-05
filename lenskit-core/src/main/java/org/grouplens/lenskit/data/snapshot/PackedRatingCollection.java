/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
/**
 *
 */
package org.grouplens.lenskit.data.snapshot;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.util.FastCollection;
import org.grouplens.lenskit.util.IntIntervalList;

/**
 * Preference collection implemented as a view on top of
 * {@link PackedRatingData}. This is used to provide the collection
 * implementations for {@link PackedRatingSnapshot}. It supports subsetting the
 * packed data set to only a particular list of indices.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class PackedRatingCollection extends AbstractCollection<IndexedPreference>
        implements FastCollection<IndexedPreference> {
    final private PackedRatingData data;
    final private IntList indices;

    /**
     * Construct a preference collection view of the entire packed data set.
     *
     * @param data A packed rating data set.
     */
    PackedRatingCollection(PackedRatingData data) {
        this.data = data;
        this.indices = new IntIntervalList(data.values.length);
    }

    /**
     * Construct a collection view of a subset of the rating data.
     *
     * @param data The rating data.
     * @param indices A list of indices in the packed data arrays to include in
     *            the collection.
     */
    PackedRatingCollection(PackedRatingData data, IntList indices) {
        this.data = data;
        this.indices = indices;
    }

    @Override
    public Iterator<IndexedPreference> iterator() {
        return new IteratorImpl();
    }

    @Override
    public int size() {
        return indices.size();
    }

    @Override
    public Iterable<IndexedPreference> fast() {
        return new Iterable<IndexedPreference>() {
            @Override
            public Iterator<IndexedPreference> iterator() {
                return fastIterator();
            }
        };
    }

    @Override
    public Iterator<IndexedPreference> fastIterator() {
        return new FastIteratorImpl();
    }

    private final class IteratorImpl implements Iterator<IndexedPreference> {
        private final IntIterator iter;

        IteratorImpl() {
            iter = indices.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public IndexedPreference next() {
            final int index = iter.next();
            return data.makeRating(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class FastIteratorImpl implements Iterator<IndexedPreference> {
        private final IntIterator iter;
        private PackedRatingData.IndirectPreference preference =
                data.makeRating(0);

        FastIteratorImpl() {
            iter = indices.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public IndexedPreference next() {
            preference.index = iter.next();
            return preference;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
