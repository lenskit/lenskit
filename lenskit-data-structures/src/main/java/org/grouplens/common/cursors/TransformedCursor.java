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
package org.grouplens.common.cursors;

import com.google.common.base.Function;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class TransformedCursor<S,T> extends AbstractCursor<T> {
	private final Cursor<S> cursor;
	private final Function<? super S, ? extends T> function;

	public TransformedCursor(Cursor<S> cursor, Function<? super S, ? extends T> function) {
	    super(cursor.getRowCount());
		this.cursor = cursor;
		this.function = function;
	}
	
	@Override
	public void close() {
		cursor.close();
	}

    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }
    
    @Override
    public T next() {
        return function.apply(cursor.next());
    }
}
