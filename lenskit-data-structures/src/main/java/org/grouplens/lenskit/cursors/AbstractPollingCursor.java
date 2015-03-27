/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

/**
 * An extension of AbstractCursor that simplifies the mechanics of the
 * next()/hasNext() implementation to a simple method, {@link #poll()}.
 *
 * @param <T>
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public abstract class AbstractPollingCursor<T> extends AbstractCursor<T> {
    private boolean hasNextCalled;
    private T polled;

    /**
     * Construct a cursor of unknown size.
     */
    public AbstractPollingCursor() {
        super();
    }

    /**
     * Construct a cursor with a known number of rows.
     *
     * @param rowCount The number of rows, or -1 for unknown size.
     */
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

    @Nonnull
    @Override
    public T next() {
        if (!hasNextCalled) {
            polled = poll();
        }
        if (polled == null) {
            throw new NoSuchElementException();
        }

        final T n = polled;
        polled = null;
        hasNextCalled = false;
        return n;
    }

    /**
     * Return the next element in this Cursor, or null if there are no more
     * elements. This must be safe to call multiple times at the end of its
     * collection.
     * <p>
     * <strong>Change in 3.0:</strong> Previously, this method was allowed to return the same object
     * repeatedly, mutated to represent the next result. This is no longer permitted.
     * </p>
     *
     * @return The next element, or null.
     */
    protected abstract T poll();
}
