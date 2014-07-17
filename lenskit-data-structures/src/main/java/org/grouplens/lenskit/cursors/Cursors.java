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
package org.grouplens.lenskit.cursors;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.WillClose;
import javax.annotation.WillCloseWhenClosed;
import java.util.*;

/**
 * Utility methods for cursors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
@SuppressWarnings("deprecation")
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

    /**
     * Create a cursor over a fixed set of elements. This is mostly useful for testing.
     * @param contents The contents.
     * @param <T> The data type.
     * @return The cursor.
     */
    public static <T> Cursor<T> of(T... contents) {
        return wrap(Arrays.asList(contents));
    }

    /**
     * Concatenate cursors.  Each cursor is closed as closed as it is consumed.
     * @param cursors The cursors to concatenate.
     * @param <T> The type of data.
     * @return The concatenated cursor.
     */
    public static <T> Cursor<T> concat(Iterable<? extends Cursor<? extends T>> cursors) {
        return new SequencedCursor<T>(cursors);
    }

    /**
     * Concatenate cursors.
     * @see #concat(Iterable)
     */
    public static <T> Cursor<T> concat(Cursor<? extends T>... cursors) {
        return concat(Arrays.asList(cursors));
    }
}
