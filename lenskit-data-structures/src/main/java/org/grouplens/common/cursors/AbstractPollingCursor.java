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

import java.util.NoSuchElementException;

/**
 * An extension of AbstractCursor that simplifies the mechanics of the
 * next()/hasNext() implementation to a simple method, poll().
 * 
 * @author Michael Ludwig
 * @param <T>
 */
public abstract class AbstractPollingCursor<T> extends AbstractCursor<T> {
    private boolean hasNextCalled;
    private T polled;
    
    public AbstractPollingCursor() {
        super();
    }
    
    public AbstractPollingCursor(int rowCount) {
        super(rowCount);
    }
    
    @Override
    public boolean hasNext() {
        if (!hasNextCalled) {
            polled = poll();
            hasNextCalled = true;
        }
        
        return polled != null;
    }

    @Override
    public T next() {
        if (!hasNextCalled)
            polled = poll();
        if (polled == null)
            throw new NoSuchElementException();
        
        T n = polled;
        polled = null;
        hasNextCalled = false;
        return n;
    }

    /**
     * Return the next element in this Cursor, or null if there are no more
     * elements. This must be safe to call multiple times at the end of its
     * collection.
     * 
     * @return The next element, or null
     */
    protected abstract T poll();
}
