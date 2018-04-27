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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link ObjectStream} that simply wraps an iterator.
 *
 * @param <T> The stream's element type.
 */
class IteratorObjectStream<T> extends AbstractObjectStream<T> {
    private final Collection<? extends T> collection;
    private final Closeable toClose;
    private Iterator<? extends T> iterator;
    private int ndone = 0;

    IteratorObjectStream(@Nonnull Collection<? extends T> col) {
        collection = col;
        iterator = col.iterator();
        toClose = null;
    }

    /**
     * Construct a new iterator stream.
     * @param iter The iterator.
     */
    IteratorObjectStream(@Nonnull Iterator<? extends T> iter) {
        Preconditions.checkNotNull(iter, "stream iterator");
        iterator = iter;
        collection = null;
        toClose = null;
    }

    /**
     * Construct a new iterator stream.
     * @param iter The iterator.
     * @param root An underlying resource to close.
     */
    IteratorObjectStream(@Nonnull Iterator<? extends T> iter, Closeable root) {
        Preconditions.checkNotNull(iter, "stream iterator");
        iterator = iter;
        collection = null;
        toClose = root;
    }

    List<T> getList() {
        if (collection != null && ndone == 0) {
            return ImmutableList.copyOf(collection);
        } else {
            return null;
        }
    }

    @Override
    public T readObject() {
        Preconditions.checkState(iterator != null, "stream has been closed");
        if (iterator.hasNext()) {
            T obj = iterator.next();
            ndone += 1;
            if (obj == null) {
                throw new NullPointerException("object stream iterator cannot contain null");
            }
            return obj;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator((Iterator<T>) iterator);
    }

    @Override
    public void close() {
        try (Closeable ignored = toClose) {
            super.close();
            iterator = null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
