/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;

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

    protected GroupingObjectStream(@WillCloseWhenClosed ObjectStream<? extends E> base) {
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
