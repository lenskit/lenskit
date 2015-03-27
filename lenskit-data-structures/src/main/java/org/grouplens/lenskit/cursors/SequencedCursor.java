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
import com.google.common.base.Throwables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of concatenated cursors.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SequencedCursor<T> extends AbstractCursor<T> {
    private final Iterator<? extends Cursor<? extends T>> cursorIter;
    @Nullable
    private Cursor<? extends T> current;

    public SequencedCursor(Iterable<? extends Cursor<? extends T>> cursors) {
        cursorIter = cursors.iterator();
        current = cursorIter.hasNext() ? cursorIter.next() : null;
    }

    @Override
    public boolean hasNext() {
        // advance
        while (current != null && !current.hasNext()) {
            current.close();
            if (cursorIter.hasNext()) {
                current = cursorIter.next();
                Preconditions.checkNotNull(current, "concatenated cursor");
            } else {
                current = null;
            }
        }
        assert current == null || current.hasNext();
        return current != null;
    }

    @Nonnull
    @Override
    public T next() {
        if (hasNext()) {
            assert current != null && current.hasNext();
            return current.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void close() {
        Throwable error = null;
        if (current != null) {
            try {
                current.close();
            } catch (Throwable th) { // NOSONAR We are managing errors
                error = th;
            }
            current = null;
        }
        while (cursorIter.hasNext()) {
            Cursor<? extends T> cur = cursorIter.next();
            try {
                cur.close();
            } catch (Throwable th) { // NOSONAR We are managing errors
                if (error != null) {
                    error = th;
                }
            }
        }
        if (error != null) {
            throw Throwables.propagate(error);
        }
    }
}
