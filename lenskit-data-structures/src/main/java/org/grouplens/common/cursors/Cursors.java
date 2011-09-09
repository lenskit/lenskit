/*
 * GroupLens Common Utilities
 * Copyright © 2011 Regents of the University of Minnesota
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of the University of Minnesota nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software has been partly funded by NSF grant IIS 08-08692.
 */
package org.grouplens.common.cursors;

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
