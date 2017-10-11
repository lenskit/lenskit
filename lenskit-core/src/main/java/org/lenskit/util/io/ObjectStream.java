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

import javax.annotation.CheckForNull;
import java.io.Closeable;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A stream of objects read from somewhere.
 *
 * This interface extends {@link Iterable} for convenience; the {@link #iterator()}
 * method does <b>not</b> return a fresh iterator but rather a wrapper of this stream.
 * It is only present to allow for-each loops over streams.  After it is exhausted,
 * any iterator returned will be null.
 *
 * @param <T> The type of data returned by the stream
 * @see AbstractObjectStream
 */
public interface ObjectStream<T> extends Closeable, Stream<T>, Iterable<T> {
    /**
     * Read the next object from this stream.
     * @return The next object, or `null` if at the end of the stream.
     */
    @CheckForNull
    T readObject();

    /**
     * Close the stream.  This invalidates the stream; no more elements may be
     * fetched after a call to {@code close()} (although implementations are
     * not required to enforce this).  It is not an error to close a stream
     * multiple times.
     */
    @Override
    void close();

    @Override
    default void forEach(Consumer<? super T> action) {
        T obj = readObject();
        while (obj != null) {
            action.accept(obj);
            obj = readObject();
        }
    }

    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
}
