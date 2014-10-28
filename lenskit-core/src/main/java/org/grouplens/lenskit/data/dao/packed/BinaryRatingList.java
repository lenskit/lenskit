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
package org.grouplens.lenskit.data.dao.packed;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.AbstractList;

/**
 * A list of ratings backed by a buffer.  This is not thread-safe.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class BinaryRatingList extends AbstractList<Rating> {
    private final BinaryFormat format;
    private final ByteBuffer buffer;
    private final IntList positions;
    private final int ratingSize;

    /**
     * Create a new binary rating list.
     * @param buf The buffer. It is duplicated, so it can be repositioned later.
     */
    public BinaryRatingList(BinaryFormat fmt, ByteBuffer buf, IntList idxes) {
        format = fmt;
        buffer = buf.duplicate();
        buffer.mark();
        positions = idxes;

        ratingSize = fmt.getRatingSize();
    }

    @Override
    public Rating get(int index) {
        int position = positions.getInt(index);
        return getRating(position);
    }

    public Rating getRating(int position) {
        buffer.reset();
        int bidx = buffer.position() + position * ratingSize;
        buffer.position(bidx);
        return format.readRating(buffer);
    }

    private void populateRating(int pos, MutableRating rating) {
        buffer.reset();
        int bidx = buffer.position() + pos * ratingSize;
        buffer.position(bidx);
        format.readRating(buffer, rating);
    }

    @Override
    public int size() {
        return positions.size();
    }

    public Cursor<Rating> cursor() {
        return new CursorImpl();
    }

    private class CursorImpl extends AbstractCursor<Rating> {
        private MutableRating rating = new MutableRating();
        private IntIterator posIter = positions.iterator();

        @Override
        public boolean hasNext() {
            return posIter.hasNext();
        }

        @Nonnull
        @Override
        public Rating next() {
            return getRating(posIter.nextInt());
        }
    }
}
