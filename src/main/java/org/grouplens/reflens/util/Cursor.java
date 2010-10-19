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

package org.grouplens.reflens.util;

import java.util.NoSuchElementException;


public interface Cursor<T> extends Iterable<T> {
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
	 * not required to enforce this).
	 */
	public void close();
}
