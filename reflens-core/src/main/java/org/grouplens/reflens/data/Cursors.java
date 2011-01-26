/*
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * 
 */
package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class Cursors {
	private static final Logger logger = LoggerFactory.getLogger(Cursors.class);
	/**
	 * Read a cursor into a list, closing when it is finished.
	 * @param <T> The type of item in the cursor.
	 * @param cursor The cursor.
	 * @return A list containing the elements of the cursor.
	 */
	public static <T> ArrayList<T> makeList(Cursor<T> cursor) {
		ArrayList<T> list;
		try {
			int n = cursor.getRowCount();
			if (n < 0) n = 10;
			list = new ArrayList<T>(n);
			for (T item: cursor) {
				list.add(item);
			}
		} finally {
			cursor.close();
		}
		
		list.trimToSize();
		return list;
	}
	
	public static LongArrayList makeList(LongCursor cursor) {
		LongArrayList list = null;
		try {
			int n = cursor.getRowCount();
			if (n < 0) n = 10;
			list = new LongArrayList(n);
			while (cursor.hasNext()) {
				list.add(cursor.nextLong());
			}
		} catch (OutOfMemoryError e) {
			logger.error("Ran out of memory with {} users",
					list == null ? -1 : list.size());
			throw e;
		} finally {
			cursor.close();
		}
		list.trim();
		return list;
	}
	
	public static <T> Cursor<T> wrap(Iterator<T> iterator) {
		return new IteratorCursor<T>(iterator);
	}
	
	public static <T> Cursor<T> wrap(Collection<T> collection) {
		return new CollectionCursor<T>(collection);
	}
	
	public static LongCursor wrap(LongIterator iter) {
		return new LongIteratorCursor(iter);
	}
	
	public static LongCursor wrap(LongCollection collection) {
		return new LongCollectionCursor(collection);
	}
	
	public static <T> LongCursor makeLongCursor(final Cursor<Long> cursor) {
		if (cursor instanceof LongCursor)
			return (LongCursor) cursor;
		
		return new LongCursor() {
			public boolean hasNext() {
				return cursor.hasNext();
			}
			public Long next() {
				return cursor.next();
			}
			public long nextLong() {
				return next();
			}
			public void close() {
				cursor.close();
			}
			public int getRowCount() {
				return cursor.getRowCount();
			}
			@Override
			public LongIterator iterator() {
				return new LongCursorIterator(this);
			}
		};
	}
	
	public static <T> Cursor<T> filter(Cursor<T> cursor, Predicate<T> predicate) {
		return new FilteredCursor<T>(cursor, predicate);
	}

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
}
