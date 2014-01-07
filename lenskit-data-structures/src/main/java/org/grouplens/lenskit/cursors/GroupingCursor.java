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

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;

/**
 * Base class for cursors that group the output of another cursor.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @param <E> The type of item in the wrapped cursor.
 * @param <C> The type of aggregate to be returned by this cursor.
 */
public abstract class GroupingCursor<C, E> extends AbstractCursor<C> {
    private final Cursor<? extends E> baseCursor;
    private E nextItem;

    protected GroupingCursor(Cursor<? extends E> base) {
        baseCursor = base;
        if (base.hasNext()) {
            nextItem = base.next();
        }
    }

    @Override
    public boolean hasNext() {
        return nextItem != null;
    }

    @Nonnull
    @Override
    public C next() {
        if (nextItem == null) {
            throw new NoSuchElementException("cursor exhausted");
        }
        C group = null;
        while (group == null && nextItem != null) {
            if (handleItem(nextItem)) {
                // item accepted, advance
                nextItem = baseCursor.hasNext() ? baseCursor.next() : null;
            } else {
                group = finishGroup();
            }
        }
        if (group == null) {
            group = finishGroup();
        }
        return group;
    }

    @Override
    public void close() {
        nextItem = null;
        try {
            clearGroup();
        } finally {
            baseCursor.close();
        }
    }

    /**
     * Clear the accumulated group.  Called by {@link #close()}.
     */
    protected abstract void clearGroup();

    /**
     * Handle an item from the base cursor.  Each item of the base cursor is passed to this method.
     * The method builds up the underlying aggregate.
     *
     * @param item The item to handle.
     * @return {@code true} if the item has been accepted and added to the group; {@code false} if
     *         it cannot be added to the current group.  If it cannot be added to the current group,
     *         the cursor will call {@link #finishGroup()} to finish the group, and then call this
     *         method again with {@code item} (at which point it is required to return {@code
     *         true}).
     */
    protected abstract boolean handleItem(E item);

    /**
     * Finish the current group and return it.  After this method has been called, {@code #handleItem(E)}
     * should add items to a new group.  This method will not be called unless {@code #handleItem(E)}
     * has been called at least once since the last group was finished.
     *
     * @return The group that has been finished.
     */
    @Nonnull
    protected abstract C finishGroup();
}
