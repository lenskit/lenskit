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
package org.grouplens.lenskit.cursors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.WillClose;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Utility methods for cursors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Cursors {

	/**
	 * Wrap an iterator in a cursor.
	 * @param <T> The type of data to return.
	 * @param iterator An iterator to wrap
	 * @return A cursor returning the elements of the iterator.
	 */
	public static <T> Cursor<T> wrap(Iterator<? extends T> iterator) {
		return new IteratorCursor<T>(iterator, -1);
	}

	/**
	 * Wrap a collection in a cursor.
	 * @param <T> The type of data to return.
	 * @param collection A collection to wrap
	 * @return A cursor returning the elements of the collection.
	 */
	public static <T> Cursor<T> wrap(Collection<? extends T> collection) {
		return new IteratorCursor<T>(collection.iterator(), collection.size());
	}

	/**
	 * Filter a cursor.
	 * @param <T> The type of cursor rows.
	 * @param cursor The source cursor.
	 * @param predicate A predicate indicating which rows to return.
	 * @return A cursor returning all rows for which <var>predicate</var> returns
	 * <tt>true</tt>.
	 */
	public static <T> Cursor<T> filter(Cursor<T> cursor, Predicate<? super T> predicate) {
		return new FilteredCursor<T>(cursor, predicate);
	}

    /**
     * Filter a cursor to only contain elements of type <var>type</var>. Unlike
     * {@link #filter(Cursor, Predicate)} with a predicate from
     * {@link Predicates#instanceOf(Class)}, this method also transforms the
     * cursor to be of the target type.
     * 
     * @param cursor The source cursor.
     * @param type The type to filter.
     * @return A cursor returning all elements in <var>cursor</var> which are
     *         instances of type <var>type</var>.
     */
	public static <T> Cursor<T> filter(final Cursor<?> cursor, final Class<T> type) {
	    return new AbstractPollingCursor<T>() {
            @SuppressWarnings("unchecked")
            @Override
            protected T poll() {
                while (cursor.hasNext()) {
                    Object obj = cursor.next();
                    if (type.isInstance(obj))
                        return (T) obj;
                }
                return null;
            }
        };
	}
	
	/**
	 * Transform a cursor's values
	 * @param <S> The type of source cursor rows
	 * @param <T> The type of output cursor rows
	 * @param cursor The source cursor
	 * @param function A function to apply to each row in the cursor.
	 * @return A new cursor iterating the results of <var>function</var>.
	 */
	public static <S,T> Cursor<T> transform(Cursor<S> cursor, Function<? super S, ? extends T> function) {
	    return new TransformedCursor<S,T>(cursor, function);
	}

	/**
	 * Create an empty cursor.
	 */
	public static <T> Cursor<T> empty() {
		return new AbstractCursor<T>() {
			@Override
			public int getRowCount() {
				return 0;
			}

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public T next() {
				throw new NoSuchElementException();
			}
		};
	}

	/**
	 * Read a cursor into a list, closing when it is finished.
	 * @param <T> The type of item in the cursor.
	 * @param cursor The cursor.
	 * @return A new list containing the elements of the cursor.  The list has been
	 * allocated with a capacity of {@link Cursor#getRowCount()} if possible,
	 * but has not been trimmed.
	 */
	public static <T> ArrayList<T> makeList(@WillClose Cursor<? extends T> cursor) {
		ArrayList<T> list;
		try {
			int n = cursor.getRowCount();
			if (n < 0) n = 20;
			list = new ArrayList<T>(n);
			for (T item: cursor) {
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
	 * @param cursor The cursor to sort.
	 * @param comp The comparator to use to sort the cursor.
	 * @return A cursor iterating over the sorted results.
	 */
	public static <T> Cursor<T> sort(@WillClose Cursor<T> cursor, Comparator<? super T> comp) {
	    ArrayList<T> list = makeList(cursor);
	    Collections.sort(list, comp);
	    return wrap(list);
	}

}
