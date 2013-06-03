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
package org.grouplens.lenskit.cursors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import it.unimi.dsi.fastutil.longs.*;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import javax.annotation.WillCloseWhenClosed;
import java.util.*;

/**
 * Utility methods for cursors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public final class Cursors {
    private static final int DEFAULT_LIST_SIZE = 20;

    private Cursors() {
    }

    /**
     * Wrap an iterator in a cursor.
     *
     * @param <T>      The type of data to return.
     * @param iterator An iterator to wrap
     * @return A cursor returning the elements of the iterator.
     */
    public static <T> Cursor<T> wrap(Iterator<? extends T> iterator) {
        return new IteratorCursor<T>(iterator, -1);
    }

    /**
     * Wrap a collection in a cursor.
     *
     * @param <T>        The type of data to return.
     * @param collection A collection to wrap
     * @return A cursor returning the elements of the collection.
     */
    public static <T> Cursor<T> wrap(Collection<? extends T> collection) {
        return new IteratorCursor<T>(collection.iterator(), collection.size());
    }

    /**
     * Filter a cursor.
     *
     * @param <T>       The type of cursor rows.
     * @param cursor    The source cursor.
     * @param predicate A predicate indicating which rows to return.
     * @return A cursor returning all rows for which {@var predicate} returns
     *         {@code true}.
     */
    public static <T> Cursor<T> filter(@WillCloseWhenClosed Cursor<T> cursor, Predicate<? super T> predicate) {
        return new FilteredCursor<T>(cursor, predicate);
    }

    /**
     * Filter a cursor to only contain elements of type {@var type}. Unlike
     * {@link #filter(Cursor, Predicate)} with a predicate from
     * {@link Predicates#instanceOf(Class)}, this method also transforms the
     * cursor to be of the target type.
     *
     * @param <T>    The type of value in the cursor.
     * @param cursor The source cursor.
     * @param type   The type to filter.
     * @return A cursor returning all elements in {@var cursor} which are
     *         instances of type {@var type}.
     */
    public static <T> Cursor<T> filter(@WillCloseWhenClosed final Cursor<?> cursor, final Class<T> type) {
        return new AbstractPollingCursor<T>() {
            @SuppressWarnings("unchecked")
            @Override
            protected T poll() {
                while (cursor.hasNext()) {
                    final Object obj = cursor.next();
                    if (type.isInstance(obj)) {
                        return (T) obj;
                    }
                }
                return null;
            }

            @Override
            public void close() {
                cursor.close();
            }
        };
    }

    /**
     * Transform a cursor's values.
     *
     * @param <S>      The type of source cursor rows
     * @param <T>      The type of output cursor rows
     * @param cursor   The source cursor
     * @param function A function to apply to each row in the cursor.
     * @return A new cursor iterating the results of {@var function}.
     */
    public static <S, T> Cursor<T> transform(@WillCloseWhenClosed Cursor<S> cursor, Function<? super S, ? extends T> function) {
        return new TransformedCursor<S, T>(cursor, function);
    }

    /**
     * Create an empty cursor.
     *
     * @param <T> The type of value in the cursor.
     * @return An empty cursor.
     */
    public static <T> Cursor<T> empty() {
        return wrap(Collections.<T>emptyList());
    }

    /**
     * Read a cursor into a list, closing when it is finished.
     *
     * @param <T>    The type of item in the cursor.
     * @param cursor The cursor.
     * @return A new list containing the elements of the cursor.  The list has been
     *         allocated with a capacity of {@link Cursor#getRowCount()} if possible,
     *         but has not been trimmed.
     */
    @SuppressWarnings("PMD.LooseCoupling")
    public static <T> ArrayList<T> makeList(@WillClose Cursor<? extends T> cursor) {
        ArrayList<T> list;
        try {
            int n = cursor.getRowCount();
            if (n < 0) {
                n = DEFAULT_LIST_SIZE;
            }
            list = new ArrayList<T>(n);
            for (T item : cursor) {
                list.add(item);
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    /**
     * Read a LongCursor into a LongArrayList, closing the cursor when done.
     *
     * @param cursor The LongCursor to be read into a list.
     * @return A new list containing the elements of the cursor. The list has been
     *         allocated with a capacity of {@link Cursor#getRowCount()} if possible,
     *         but has not been trimmed.
     */
    public static LongArrayList makeList(@WillClose LongCursor cursor) {
        LongArrayList list = null;
        try {
            int n = cursor.getRowCount();
            if (n < 0) {
                n = DEFAULT_LIST_SIZE;
            }
            list = new LongArrayList(n);
            while (cursor.hasNext()) {
                list.add(cursor.nextLong());
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    /**
     * Read a LongCursor into a LongSet, closing the cursor when done.
     *
     * @param cursor The LongCursor to be read into a set.
     * @return A LongSet containing the elements of the cursor.
     */
    public static LongSet makeSet(@WillClose LongCursor cursor) {
        LongOpenHashSet set = null;
        try {
            int n = cursor.getRowCount();
            if (n < 0) {
                n = DEFAULT_LIST_SIZE;
            }
            set = new LongOpenHashSet(n);
            while (cursor.hasNext()) {
                set.add(cursor.nextLong());
            }
        } finally {
            cursor.close();
        }
        set.trim();
        return set;
    }

    /**
     * Wrap a long iterator in a cursor.
     *
     * @param iter An iterator.
     * @return A cursor backed by {@code iter}. Closing the cursor is a no-op.
     */
    public static LongCursor wrap(LongIterator iter) {
        return new LongIteratorCursor(iter);
    }

    /**
     * Construct a cursor over a collection.
     *
     * @param collection A collection.
     * @return A cursor over the collection. Closing the cursor is a no-op.
     */
    public static LongCursor wrap(LongCollection collection) {
        return new LongIteratorCursor(collection.iterator(), collection.size());
    }

    /**
     * Make a fast long cursor.
     *
     * @param cursor A cursor.
     * @return A {@link LongCursor}. If {@code cursor} is a LongCursor, it is returned.
     */
    public static LongCursor makeLongCursor(@WillCloseWhenClosed final Cursor<Long> cursor) {
        if (cursor instanceof LongCursor) {
            return (LongCursor) cursor;
        } else {
            return new UnboxingLongCursor(cursor);
        }
    }

    /**
     * Sort a cursor.  This reads the original cursor into a list, sorts it, and
     * returns a new cursor backed by the list (after closing the original cursor).
     *
     * @param <T>    The type of value in the cursor.
     * @param cursor The cursor to sort.
     * @param comp   The comparator to use to sort the cursor.
     * @return A cursor iterating over the sorted results.
     */
    public static <T> Cursor<T> sort(@WillClose Cursor<T> cursor,
                                     Comparator<? super T> comp) {
        final ArrayList<T> list = makeList(cursor);
        Collections.sort(list, comp);
        return wrap(list);
    }

    private static class UnboxingLongCursor implements LongCursor {
        private final Cursor<Long> cursor;

        public UnboxingLongCursor(Cursor<Long> cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return cursor.hasNext();
        }

        @Nonnull
        @Override
        public Long next() {
            return cursor.next();
        }

        @Nonnull
        @Override
        public Long fastNext() {
            return cursor.fastNext();
        }

        @Override
        public LongIterable fast() {
            return this;
        }

        @Override
        public long nextLong() {
            return next();
        }

        @Override
        public void close() {
            cursor.close();
        }

        @Override
        public int getRowCount() {
            return cursor.getRowCount();
        }

        @Override
        public LongIterator iterator() {
            return new LongCursorIterator(this);
        }
    }
}
