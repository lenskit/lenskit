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

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Implementation of {@link Cursor} that simply wraps an iterator.
 *
 * @param <T> The cursor's element type.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class IteratorCursor<T> extends AbstractCursor<T> {
    private Iterator<? extends T> iterator;

    /**
     * Construct a new iterator cursor.
     * @param iter The iterator.
     * @param size The length, or -1 if the length is not known.  This length must be an upper
     *             bound on the cursor's element count.
     */
    public IteratorCursor(@Nonnull Iterator<? extends T> iter, int size) {
        super(size);
        Preconditions.checkNotNull(iter, "iterator for cursor");
        iterator = iter;
    }

    @Override
    public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    @Nonnull
    @Override
    public T next() {
        if (iterator == null) {
            throw new IllegalStateException("cursor closed");
        }

        return iterator.next();
    }

    @Override
    public void close() {
        super.close();
        iterator = null;
    }
}
