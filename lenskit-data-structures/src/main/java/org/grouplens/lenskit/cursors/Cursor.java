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
import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Cursors over data connections.  These are basically closable iterators which
 * also implement {@link Iterable} for convenience.  Cursors are not allowed to
 * contain null elements.
 *
 * <p>
 * Note that the {@link #iterator()} method does <b>not</b> return a fresh
 * iterator but rather a wrapper of this cursor; it is only present to allow
 * for-each loops over cursors.  After it is exhausted, any iterator returned
 * will be null.
 *
 * <p>
 * This class does not implement {@link Iterator} because the 'is-a' relationship
 * does not hold; cursors must be closed by their clients while iterators do
 * not have such a requirement.
 *
 * @param <T> The type of data returned by the cursor
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface Cursor<T> extends Iterable<T>, Closeable {
    /**
     * Get an upper bound on the number of rows available from the cursor.
     *
     * @return the maximum number of rows which may be returned by {@link #next()},
     *         or -1 if that count is not available.
     */
    int getRowCount();

    /**
     * Query whether the cursor has any more items.  If the cursor or underlying
     * source has been closed, this may return even if the end has not been
     * reached.
     *
     * @return {@code true} if there remains another item to fetch.
     */
    boolean hasNext();

    /**
     * Fetch the next item from the cursor.
     *
     * @return The next item in the cursor.
     * @throws NoSuchElementException if there are no more elements.
     * @throws RuntimeException       if the cursor or its data source have been
     *                                closed (optional).
     */
    @Nonnull
    T next();

    /**
     * Close the cursor.  This invalidates the cursor; no more elements may be
     * fetched after a call to {@code close()} (although implementations are
     * not required to enforce this).  It is not an error to close a cursor
     * multiple times.
     */
    @Override
    void close();
}
