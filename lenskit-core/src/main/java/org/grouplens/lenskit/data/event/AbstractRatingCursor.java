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
package org.grouplens.lenskit.data.event;

import java.util.NoSuchElementException;

import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.AbstractPollingCursor;

/**
 * Polling-based cursor implementation for ratings with fast poll methods.
 *
 * <p>This is only for cursors which support mutating rating objects.  The ratings
 * are cloned to implement {@link #next()}.  If your {@link #poll()} method returns
 * fresh rating objects for each rating, extend {@link AbstractPollingCursor} instead.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractRatingCursor<R extends Rating> extends AbstractCursor<R> {
    /**
     * Poll for the next rating. This implementation should mutate and re-use
     * the same rating object if possible.
     *
     * @return The rating, or <tt>null</tt> if at the end of the cursor.
     */
    protected abstract R poll();

    private boolean hasNextCalled;
    private R polled;

    public AbstractRatingCursor() {
        super();
    }

    public AbstractRatingCursor(int rowCount) {
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
    public R fastNext() {
        if (!hasNextCalled)
            polled = poll();
        if (polled == null)
            throw new NoSuchElementException();

        R n = polled;
        polled = null;
        hasNextCalled = false;
        return n;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R next() {
        return (R) fastNext().clone();
    }
}
