/* RefLens, a reference implementation of recommender algorithms.
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
 */

package org.grouplens.reflens.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cursors over data connections.  These are basically closable iterators which
 * also implement {@link Iterable} for convenience.
 * 
 * Note that the {@link #iterator()} method does <b>not</b> return a fresh
 * iterator but rather a wraper of this cursor; it is only present to allow
 * for-each loops over cursors.  After it is exhausted, any iterator returned
 * will be null.
 * 
 * This class does not implement {@link Iterator} because the 'is-a' relationship
 * does not hold; cursors must be closed by their clients while iterators do
 * not have such a requirement.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <T> The type of data returned by the cursor
 */
public interface Cursor<T> extends Iterable<T> {
	/**
	 * Get an upper bound on the number of rows available from the cursor.
	 * @return the number of rows which may be returned by {@link #next()}, or
	 * -1 if that count is not available.
	 */
	public int getRowCount();

	/**
	 * Query whether the cursor has any more items.  If the cursor or underlying
	 * source has been closed, this may return even if the end has not been
	 * reached.
	 * @return <tt>true</tt> if there remains another item to fetch.
	 */
	public boolean hasNext();
	
	/**
	 * Fetch the next item from the cursor.
	 * @return The next item in the cursor.
	 * @throws NoSuchElementException if there are no more elements.
	 * @throws RuntimeException if the cursor or its data source have been
	 * closed (optional).
	 */
	public T next();
	
	/**
	 * Close the cursor.  This invalidates the cursor; no more elements may be
	 * fetched after a call to <tt>close()</tt> (although implementations are
	 * not required to enforce this).  It is not an error to close a cursor
	 * multiple times.
	 */
	public void close();
}
