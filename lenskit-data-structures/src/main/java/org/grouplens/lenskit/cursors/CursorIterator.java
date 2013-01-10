/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
 * Simple implementation of an Iterator that wraps a Cursor's data. This is
 * suitable for use with {@link Cursor#iterator()}.
 *
 * @param <T> The type of value in the iterator.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public class CursorIterator<T> implements Iterator<T> {

    private Cursor<T> cursor;

    /**
     * Construct a new iterator from a cursor.
     *
     * @param cur The cursor to wrap.
     */
    public CursorIterator(Cursor<T> cur) {
        cursor = cur;
    }

    /* (non-Javadoc)
      * @see java.util.Iterator#hasNext()
      */
    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }

    /* (non-Javadoc)
      * @see java.util.Iterator#next()
      */
    @Override
    public T next() {
        return cursor.next();
    }

    /* (non-Javadoc)
      * @see java.util.Iterator#remove()
      */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
