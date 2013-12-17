package org.grouplens.lenskit.data.dao.packed;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Iterator;

/**
 * A list of ratings backed by a buffer.  This is not thread-safe.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BinaryRatingList extends AbstractList<Rating> implements FastCollection<Rating> {
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
        int position = positions.get(index);
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

    @Override
    public Iterator<Rating> fastIterator() {
        return new FastIterImpl();
    }

    public Cursor<Rating> cursor() {
        return new CursorImpl();
    }

    private class FastIterImpl implements Iterator<Rating> {
        private MutableRating rating = new MutableRating();
        private IntIterator posIter = positions.iterator();

        @Override
        public boolean hasNext() {
            return posIter.hasNext();
        }

        @Override
        public Rating next() {
            int position = posIter.nextInt();
            populateRating(position, rating);
            return rating;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove not supported");
        }
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

        @Nonnull
        @Override
        public Rating fastNext() {
            populateRating(posIter.nextInt(), rating);
            return rating;
        }
    }
}
