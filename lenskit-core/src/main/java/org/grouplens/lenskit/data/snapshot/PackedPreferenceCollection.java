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
/**
 *
 */
package org.grouplens.lenskit.data.snapshot;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.pref.IndexedPreference;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * Preference collection implemented as a view on top of
 * {@link PackedPreferenceData}. This is used to provide the collection
 * implementations for {@link PackedPreferenceSnapshot}. It supports subsetting the
 * packed data set to only a particular list of indices.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("javadoc")
        // JavaDoc warnings incorrectly flag PackedPreferenceData
class PackedPreferenceCollection extends AbstractCollection<IndexedPreference>
        implements FastCollection<IndexedPreference> {
    private final PackedPreferenceData data;
    private final IntList indices;

    /**
     * Construct a preference collection view of the entire packed data set.
     *
     * @param data A packed rating data set.
     */
    PackedPreferenceCollection(PackedPreferenceData data) {
        this(data, CollectionUtils.interval(0, data.size()));
    }

    /**
     * Construct a collection view of a subset of the rating data.
     *
     * @param data    The rating data.
     * @param indices A list of indices in the packed data arrays to include in
     *                the collection.
     */
    PackedPreferenceCollection(PackedPreferenceData data, IntList indices) {
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
            final int index = iter.nextInt();
            return data.preference(index);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final class FastIteratorImpl implements Iterator<IndexedPreference> {
        private final IntIterator iter;
        private PackedPreferenceData.IndirectPreference preference;

        FastIteratorImpl() {
            iter = indices.iterator();
            preference = data.preference(0);
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public IndexedPreference next() {
            preference.setIndex(iter.nextInt());
            assert preference.isValid();
            return preference;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
