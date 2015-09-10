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

import javax.annotation.CheckForNull;
import java.io.Closeable;

/**
 * A stream of objects read from somewhere.
 *
 * This interface extends {@link Iterable} for convenience; the {@link #iterator()}
 * method does <b>not</b> return a fresh iterator but rather a wrapper of this stream.
 * It is only present to allow for-each loops over streams.  After it is exhausted,
 * any iterator returned will be null.
 *
 * @param <T> The type of data returned by the stream
 */
public interface ObjectStream<T> extends Iterable<T>, Closeable {
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
}
