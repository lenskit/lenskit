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

import java.util.Iterator;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractCursor<T> implements Cursor<T> {
	@Override
	public int getRowCount() {
		return -1;
	}
	
	/**
	 * No-op implementation of the {@link Cursor#close()} method.
	 */
	@Override
	public void close() {
		// no-op
	}

	/**
	 * Get the iterator.  This method just returns <tt>this</tt>, so for-each
	 * loops can be used over cursors.
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new CursorIterator<T>(this);
	}
}
