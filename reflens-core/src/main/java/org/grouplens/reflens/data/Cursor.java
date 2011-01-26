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

package org.grouplens.reflens.data;

import java.io.Closeable;
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
public interface Cursor<T> extends Iterable<T>, Closeable {
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
