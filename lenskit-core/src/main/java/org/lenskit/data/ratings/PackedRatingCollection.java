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
/**
 *
 */
package org.lenskit.data.ratings;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import org.grouplens.lenskit.collections.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.AbstractList;
import java.util.ListIterator;

/**
 * Preference collection implemented as a view on top of
 * {@link PackedRatingData}. This is used to provide the collection
 * implementations for {@link PackedRatingMatrix}. It supports subsetting the
 * packed data set to only a particular list of indices.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings({"javadoc"})
        // JavaDoc warnings incorrectly flag PackedPreferenceData
class PackedRatingCollection extends AbstractList<RatingMatrixEntry> {
    private final PackedRatingData data;
    private final IntList indices;

    /**
     * Construct a preference collection view of the entire packed data set.
     *
     * @param data A packed rating data set.
     */
    PackedRatingCollection(PackedRatingData data) {
        this(data, CollectionUtils.interval(0, data.size()));
    }

    /**
     * Construct a collection view of a subset of the rating data.
     *
     * @param data    The rating data.
     * @param indices A list of indices in the packed data arrays to include in
     *                the collection.
     */
    PackedRatingCollection(PackedRatingData data, IntList indices) {
        this.data = data;
        this.indices = indices;
    }

    @Nonnull
    @Override
    public ListIterator<RatingMatrixEntry> iterator() {
        return new IteratorImpl();
    }

    @Override
    public RatingMatrixEntry get(int index) {
        Preconditions.checkElementIndex(index, indices.size());
        return data.getEntry(index);
    }

    @Override
    public int size() {
        return indices.size();
    }

    private final class IteratorImpl implements ListIterator<RatingMatrixEntry> {
        private final IntListIterator iter;

        IteratorImpl() {
            iter = indices.iterator();
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public RatingMatrixEntry next() {
            final int index = iter.nextInt();
            return data.getEntry(index);
        }

        @Override
        public boolean hasPrevious() {
            return iter.hasPrevious();
        }

        @Override
        public RatingMatrixEntry previous() {
            final int index = iter.previousInt();
            return data.getEntry(index);
        }

        @Override
        public int nextIndex() {
            return iter.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iter.previousIndex();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(RatingMatrixEntry ratingMatrixEntry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(RatingMatrixEntry ratingMatrixEntry) {
            throw new UnsupportedOperationException();
        }
    }
}
