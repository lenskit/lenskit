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
    public T fastNext() {
        if (!hasNextCalled) {
            polled = poll();
        }
        if (polled == null) {
            throw new NoSuchElementException();
        }
        
        T n = polled;
        polled = null;
        hasNextCalled = false;
        return n;
    }

    @Override
    public T next() {
        return copy(fastNext());
    }

    /**
     * Return the next element in this Cursor, or null if there are no more
     * elements. This must be safe to call multiple times at the end of its
     * collection.
     * 
     * @return The next element, or null
     */
    protected abstract T poll();

    /**
     * Construct a copy of an object. The default does not copy; it just returns
     * its parameter. If a subclass wants to fast iteration to actually be fast
     * (mutate and reuse the same object), it should do so in its {@link #poll()}
     * method, and override this method to create a new copy of an object for when
     * {@link #next()} is called. Subclasses which return fresh objects on each
     * call to {@link #poll()} do not need to override this method.
     * @param obj The object to copy.
     * @return A copy of {@code obj}.
     */
    protected T copy(T obj) {
        return obj;
    }
}
