/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
/**
 *
 */
package org.lenskit.data.ratings;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import org.lenskit.util.collections.CollectionUtils;

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
