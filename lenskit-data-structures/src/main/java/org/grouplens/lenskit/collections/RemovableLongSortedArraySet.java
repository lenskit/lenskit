package org.grouplens.lenskit.collections;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.*;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

/**
 * Version of {@link LongSortedArraySet} that supports mutation.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class RemovableLongSortedArraySet extends LongSortedArraySet {
    private static final long serialVersionUID = 1L;

    RemovableLongSortedArraySet(@Nonnull LongKeyDomain ks) {
        super(ks);
    }

    @Override
    public boolean remove(long k) {
        int idx = keys.getIndexIfActive(k);
        if (idx >= 0) {
            keys.setActive(idx, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove all items in the specified iterator.
     * @param iter An iterator of items to remove (in sorted order).
     * @return {@code true} if any items were removed.
     */
    private boolean removeAll(LongIterator iter) {
        boolean removed = false;
        IntPointer posPtr = keys.activeIndexPointer();
        while (iter.hasNext() && !posPtr.isAtEnd()) {
            long rmk = iter.nextLong();
            // advance position pointer looking for this item
            while (!posPtr.isAtEnd() && keys.getKey(posPtr.getInt()) < rmk) {
                posPtr.advance();
            }
            // remove if necessary
            if (!posPtr.isAtEnd() && keys.getKey(posPtr.getInt()) == rmk) {
                keys.setActive(posPtr.getInt(), false);
                removed = true;
            }
        }
        return removed;
    }

    @Override
    public boolean removeAll(LongCollection c) {
        if (c instanceof LongSortedSet) {
            return removeAll(c.iterator());
        } else {
            long[] longs = c.toLongArray();
            Arrays.sort(longs);
            return removeAll(LongArrayList.wrap(longs).iterator());
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (c instanceof LongCollection) {
            return removeAll((LongCollection) c);
        } else {
            long[] longs = LongIterators.unwrap(LongIterators.asLongIterator(Iterators.filter(c.iterator(), Long.class)));
            Arrays.sort(longs);
            return removeAll(LongArrayList.wrap(longs).iterator());
        }
    }

    /**
     * Retain all items in the specified iterator.
     * @param iter An iterator of items to not remove (in sorted order).
     * @return {@code true} if any items were removed.
     */
    private boolean retainAll(LongIterator iter) {
        boolean removed = false;
        IntPointer posPtr = keys.activeIndexPointer();
        while (iter.hasNext() && !posPtr.isAtEnd()) {
            long rmk = iter.nextLong();
            // advance position pointer looking for this item
            while (!posPtr.isAtEnd() && keys.getKey(posPtr.getInt()) < rmk) {
                // this item is < than rmk, delete it
                keys.setActive(posPtr.getInt(), false);
                removed = true;
                posPtr.advance();
            }
            // skip over the item, to prepare for next
            if (!posPtr.isAtEnd() && keys.getKey(posPtr.getInt()) == rmk) {
                posPtr.advance();
            }
        }
        return removed;
    }

    @Override
    public boolean retainAll(LongCollection c) {
        if (c instanceof LongSortedSet) {
            return retainAll(c.iterator());
        } else {
            long[] longs = c.toLongArray();
            Arrays.sort(longs);
            return retainAll(LongArrayList.wrap(longs).iterator());
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (c instanceof LongCollection) {
            return retainAll((LongCollection) c);
        } else {
            long[] longs = LongIterators.unwrap(LongIterators.asLongIterator(Iterators.filter(c.iterator(), Long.class)));
            Arrays.sort(longs);
            return retainAll(LongArrayList.wrap(longs).iterator());
        }
    }
}
