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
package org.grouplens.lenskit.collections;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.ints.IntIterator;
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
        IntIterator posIter = keys.activeIndexIterator(true);
        int idx = posIter.hasNext() ? posIter.nextInt() : -1;
        while (iter.hasNext() && idx >= 0) {
            long rmk = iter.nextLong();
            // advance position pointer looking for this item
            while (idx >= 0 && keys.getKey(idx) < rmk) {
                idx = posIter.hasNext() ? posIter.nextInt() : -1;
            }
            // remove if necessary
            if (idx >= 0 && keys.getKey(idx) == rmk) {
                keys.setActive(idx, false);
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
        IntIterator posIter = keys.activeIndexIterator(true);
        int pos = posIter.hasNext() ? posIter.nextInt() : -1;
        while (iter.hasNext() && pos >= 0) {
            long rmk = iter.nextLong();
            // advance position pointer looking for this item
            while (pos >= 0 && keys.getKey(pos) < rmk) {
                // this item is < than rmk, delete it
                keys.setActive(pos, false);
                removed = true;
                pos = posIter.hasNext() ? posIter.nextInt() : -1;
            }
            // skip over the item, to prepare for next
            if (pos >= 0 && keys.getKey(pos) == rmk) {
                pos = posIter.hasNext() ? posIter.nextInt() : -1;
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
