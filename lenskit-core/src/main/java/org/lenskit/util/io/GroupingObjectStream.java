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
package org.lenskit.util.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class for streams that group the output of another stream.
 *
 * @since 2.1
 * @param <E> The type of item in the wrapped stream.
 * @param <C> The type of aggregate to be returned by this stream.
 */
public abstract class GroupingObjectStream<C, E> extends AbstractObjectStream<C> {
    private final ObjectStream<? extends E> baseStream;
    private E nextItem;

    protected GroupingObjectStream(ObjectStream<? extends E> base) {
        baseStream = base;
        // prime the stream;
        nextItem = baseStream.readObject();
    }

    @Nullable
    @Override
    public C readObject() {
        if (nextItem == null) {
            return null;
        }
        C group = null;
        while (group == null && nextItem != null) {
            if (handleItem(nextItem)) {
                // item accepted, advance
                nextItem = baseStream.readObject();
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
            baseStream.close();
        }
    }

    /**
     * Clear the accumulated group.  Called by {@link #close()}.
     */
    protected abstract void clearGroup();

    /**
     * Handle an item from the base stream.  Each item of the base stream is passed to this method.
     * The method builds up the underlying aggregate.
     *
     * @param item The item to handle.
     * @return {@code true} if the item has been accepted and added to the group; {@code false} if
     *         it cannot be added to the current group.  If it cannot be added to the current group,
     *         the stream will call {@link #finishGroup()} to finish the group, and then call this
     *         method again with {@code item} (at which point it is required to return {@code
     *         true}).
     */
    protected abstract boolean handleItem(@Nonnull E item);

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
