/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of {@link Cursor} that simply wraps an iterator.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class IteratorCursor<T> extends AbstractCursor<T> {
	private Iterator<? extends T> iterator;

	public IteratorCursor(Iterator<? extends T> iter, int size) {
	    super(size);
		iterator = iter;
	}

	@Override
	public boolean hasNext() {
		return iterator != null && iterator.hasNext();
	}

	@Override
	public T next() {
	    if (iterator == null)
	        throw new NoSuchElementException();
	    
		return iterator.next();
	}
	
	@Override
	public Iterator<T> iterator() {
	    if (iterator == null)
	        throw new IllegalStateException("cursor closed");
		return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
	}
	
	@Override
	public void close() {
	    super.close();
	    iterator = null;
	}
}
