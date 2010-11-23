/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
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
