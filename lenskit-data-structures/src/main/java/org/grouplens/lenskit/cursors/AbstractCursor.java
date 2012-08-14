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

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
public abstract class AbstractCursor<T> implements Cursor<T> {
    private final int rowCount;
    
    public AbstractCursor() {
        this(-1);
    }
    
    public AbstractCursor(int rowCount) {
        this.rowCount = Math.max(rowCount, -1); // Just a convenience to make all neg. #s map to -1
    }
    
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	/**
	 * No-op implementation of the {@link Cursor#close()} method.
	 */
	@Override
	public void close() {
		// no-op
	}
	
	/**
	 * Implementation of {@link Cursor#fastNext()} that delegates to {@link #next()}.
	 */
	@Override
    public T fastNext() {
	    return next();
	}
	
	@Override
    public Iterable<T> fast() {
	    return new Iterable<T>() {
	        @Override
            public Iterator<T> iterator() {
	            return new Iterator<T>() {
	                @Override
                    public boolean hasNext() {
	                    return AbstractCursor.this.hasNext();
	                }
	                @Override
                    public T next() {
	                    return fastNext();
	                }
	                @Override
                    public void remove() {
	                    throw new UnsupportedOperationException();
	                }
	            };
	        }
	    };
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
